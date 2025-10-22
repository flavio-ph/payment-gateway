package com.nimble.paymentgateway.domain.service.Imp;

import com.nimble.paymentgateway.api.dto.PagamentoRequestDTO;
import com.nimble.paymentgateway.api.dto.PagamentoResponseDTO;
import com.nimble.paymentgateway.domain.exception.BusinessValidationException;
import com.nimble.paymentgateway.domain.exception.PagamentoException;
import com.nimble.paymentgateway.domain.exception.ResourceNotFoundException;
import com.nimble.paymentgateway.domain.model.Cobranca;
import com.nimble.paymentgateway.domain.model.Conta;
import com.nimble.paymentgateway.domain.model.Pagamento;
import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import com.nimble.paymentgateway.domain.model.enums.StatusPagamento;
import com.nimble.paymentgateway.domain.repository.CobrancaRepository;
import com.nimble.paymentgateway.domain.repository.ContaRepository;
import com.nimble.paymentgateway.domain.repository.PagamentoRepository;
import com.nimble.paymentgateway.domain.repository.UsuarioRepository;
// Import removido (ContaService não estava sendo usado)
import com.nimble.paymentgateway.domain.service.PagamentoService;
import com.nimble.paymentgateway.infra.integration.AuthorizerClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagamentoServiceImpl implements PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final CobrancaRepository cobrancaRepository;
    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthorizerClient authorizerClient;

    @Override
    @Transactional
    public PagamentoResponseDTO processarPagamento(PagamentoRequestDTO dto, String cpfPagador) {

        Cobranca cobranca = cobrancaRepository.findById(dto.idCobranca())
                .orElseThrow(() -> new ResourceNotFoundException("Cobrança não encontrada."));

        Usuario pagador = usuarioRepository.findByCpf(cpfPagador)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário pagador não encontrado."));

        if (cobranca.getStatus() != StatusCobranca.PENDENTE) {
            throw new BusinessValidationException("Cobrança não está pendente. Status: " + cobranca.getStatus());
        }

        if (!cobranca.getCpfDestino().equals(pagador.getCpf())) {
            throw new BusinessValidationException("Este usuário não é o destinatário da cobrança.");
        }

        if (dto.metodoPagamento() == MetodoPagamento.SALDO) {
            return pagarComSaldo(cobranca, pagador);
        } else {
            return pagarComCartao(cobranca, pagador, dto);
        }
    }

    private PagamentoResponseDTO pagarComSaldo(Cobranca cobranca, Usuario pagador) {
        Conta contaPagador = contaRepository.findWithLockByUsuario_Id(pagador.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta do pagador não encontrada."));

        if (contaPagador.getSaldo().compareTo(cobranca.getValor()) < 0) {
            throw new BusinessValidationException("Saldo insuficiente.");
        }

        Conta contaRecebedor = contaRepository.findWithLockByUsuario_Id(cobranca.getUsuarioOrigem().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta do recebedor não encontrada."));

        contaPagador.setSaldo(contaPagador.getSaldo().subtract(cobranca.getValor()));
        contaRecebedor.setSaldo(contaRecebedor.getSaldo().add(cobranca.getValor()));

        contaRepository.save(contaPagador);
        contaRepository.save(contaRecebedor);

        return registrarPagamento(cobranca, pagador, MetodoPagamento.SALDO, null, StatusPagamento.APROVADO);
    }

    // Alteração: Método agora recebe PagamentoRequestDTO
    private PagamentoResponseDTO pagarComCartao(Cobranca cobranca, Usuario pagador, PagamentoRequestDTO dto) {


        if (dto.cardDetails() == null) {
            throw new BusinessValidationException("Os detalhes do cartão (número, data de expiração, CVV) são obrigatórios para pagamento com cartão.");
        }

        if (!authorizerClient.authorize()) {
            throw new PagamentoException("Pagamento com cartão de crédito não autorizado.");
        }

        return registrarPagamento(cobranca, pagador, MetodoPagamento.CARTAO, UUID.randomUUID().toString(), StatusPagamento.APROVADO);
    }

    @Override
    @Transactional
    public void cancelarPagamento(Long cobrancaId) {
        Cobranca cobranca = cobrancaRepository.findById(cobrancaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cobrança não encontrada."));

        Pagamento pagamento = pagamentoRepository.findByCobranca_Id(cobrancaId).stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento associado não encontrado."));

        if (pagamento.getStatus() == StatusPagamento.ESTORNADO) {
            throw new BusinessValidationException("Este pagamento já foi estornado.");
        }

        if (pagamento.getMetodo() == MetodoPagamento.SALDO) {
            estornarSaldo(cobranca, pagamento);
        } else {
            estornarCartao(pagamento);
        }

        cobranca.setStatus(StatusCobranca.CANCELADA);
        cobrancaRepository.save(cobranca);

        pagamento.setStatus(StatusPagamento.ESTORNADO);
        pagamentoRepository.save(pagamento);
    }

    private void estornarSaldo(Cobranca cobranca, Pagamento pagamento) {
        Conta contaPagador = contaRepository.findWithLockByUsuario_Id(pagamento.getUsuarioPagador().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta do pagador não encontrada."));

        Conta contaRecebedor = contaRepository.findWithLockByUsuario_Id(cobranca.getUsuarioOrigem().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta do recebedor não encontrada."));

        contaPagador.setSaldo(contaPagador.getSaldo().add(pagamento.getValor()));
        contaRecebedor.setSaldo(contaRecebedor.getSaldo().subtract(pagamento.getValor()));

        contaRepository.save(contaPagador);
        contaRepository.save(contaRecebedor);
    }

    private void estornarCartao(Pagamento pagamento) {
        if (!authorizerClient.authorize()) {
            throw new PagamentoException("Estorno de cartão de crédito não autorizado pelo serviço externo.");
        }
    }

    @Override
    public PagamentoResponseDTO buscarPorId(Long id) {
        Pagamento pag = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado."));
        return mapToDTO(pag);
    }

    @Override
    public PagamentoResponseDTO efetuarPagamento(PagamentoRequestDTO dto) {
        // Método obsoleto, use processarPagamento(dto, cpf)
        return null;
    }

    private PagamentoResponseDTO registrarPagamento(Cobranca cobranca, Usuario pagador, MetodoPagamento metodo, String refExterna, StatusPagamento status) {
        cobranca.setStatus(StatusCobranca.PAGA);
        cobranca.setDataPagamento(LocalDateTime.now());
        cobranca.setMetodoPagamento(metodo);
        cobrancaRepository.save(cobranca);

        Pagamento pagamento = new Pagamento();
        pagamento.setCobranca(cobranca);
        pagamento.setUsuarioPagador(pagador);
        pagamento.setValor(cobranca.getValor());
        pagamento.setMetodo(metodo);
        pagamento.setStatus(status);
        pagamento.setReferenciaExterna(refExterna);

        Pagamento salvo = pagamentoRepository.save(pagamento);
        return mapToDTO(salvo);
    }

    private PagamentoResponseDTO mapToDTO(Pagamento p) {
        return new PagamentoResponseDTO(
                p.getId(),
                p.getCobranca().getId(),
                p.getUsuarioPagador().getId(),
                p.getValor(),
                p.getMetodo(),
                p.getStatus(),
                p.getReferenciaExterna(),
                p.getDataCriacao()
        );
    }
}