package com.nimble.paymentgateway.domain.service.Imp;
import com.nimble.paymentgateway.api.dto.CobrancaRequestDTO;
import com.nimble.paymentgateway.api.dto.CobrancaResponseDTO;
import com.nimble.paymentgateway.domain.exception.BusinessValidationException;
import com.nimble.paymentgateway.domain.exception.ResourceNotFoundException;
import com.nimble.paymentgateway.domain.model.Cobranca;
import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import com.nimble.paymentgateway.domain.repository.CobrancaRepository;
import com.nimble.paymentgateway.domain.repository.UsuarioRepository;
import com.nimble.paymentgateway.domain.service.CobrancaService;
import com.nimble.paymentgateway.domain.service.PagamentoService; // Necessário para estorno
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors; // Importar Collectors

@Service
@RequiredArgsConstructor
public class CobrancaServiceImpl implements CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagamentoService pagamentoService;

    @Override
    @Transactional
    public CobrancaResponseDTO criarCobranca(CobrancaRequestDTO dto, String cpfOrigem) {

        Usuario usuarioOrigem = usuarioRepository.findByCpf(cpfOrigem)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário de origem não encontrado."));

        if (!usuarioRepository.existsByCpf(dto.cpfDestino())) {
            throw new ResourceNotFoundException("Usuário destinatário (CPF " + dto.cpfDestino() + ") não encontrado.");
        }

        if (cpfOrigem.equals(dto.cpfDestino())) {
            throw new BusinessValidationException("Usuário de origem e destino não podem ser os mesmos.");
        }

        Cobranca cobranca = new Cobranca();
        cobranca.setUsuarioOrigem(usuarioOrigem);
        cobranca.setCpfDestino(dto.cpfDestino());
        cobranca.setValor(dto.valor());
        cobranca.setDescricao(dto.descricao());
        cobranca.setMetodoPagamento(dto.metodoPagamento());

        Cobranca cobrancaSalva = cobrancaRepository.save(cobranca);

        return mapToDTO(cobrancaSalva);
    }

    @Override
    public CobrancaResponseDTO buscarPorId(Long id) {
        Cobranca cobranca = cobrancaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cobrança não encontrada."));
        return mapToDTO(cobranca);
    }

    @Override
    @Transactional
    public void cancelarCobranca(Long id, String cpfSolicitante) {
        Cobranca cobranca = cobrancaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cobrança não encontrada."));

        Usuario solicitante = usuarioRepository.findByCpf(cpfSolicitante)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário solicitante não encontrado."));

        if (!cobranca.getUsuarioOrigem().getId().equals(solicitante.getId())) {
            throw new BusinessValidationException("Apenas o criador da cobrança pode cancelá-la.");
        }

        if (cobranca.getStatus() == StatusCobranca.PAGA) {
            pagamentoService.cancelarPagamento(id);
        } else if (cobranca.getStatus() == StatusCobranca.PENDENTE) {
            cobranca.setStatus(StatusCobranca.CANCELADA);
            cobrancaRepository.save(cobranca);
        } else {
            throw new BusinessValidationException("Cobrança não pode ser cancelada (Status: " + cobranca.getStatus() + ")");
        }
    }

    @Override
    public List<CobrancaResponseDTO> listarCobrancasPorStatus(String cpfUsuario, StatusCobranca status, boolean enviadas) {
        if (enviadas) {
            Usuario origem = usuarioRepository.findByCpf(cpfUsuario)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
            return cobrancaRepository.findByUsuarioOrigemAndStatus(origem, status)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } else {
            return cobrancaRepository.findByCpfDestinoAndStatus(cpfUsuario, status)
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }
    }

    private CobrancaResponseDTO mapToDTO(Cobranca c) {
        // ... (método mapToDTO como antes) ...
        return new CobrancaResponseDTO(
                c.getId(),
                c.getUsuarioOrigem() != null ? c.getUsuarioOrigem().getId() : null,
                c.getCpfDestino(),
                c.getValor(),
                c.getDescricao(),
                c.getMetodoPagamento(),
                c.getStatus(),
                c.getDataCriacao(),
                c.getDataPagamento()
        );
    }
}