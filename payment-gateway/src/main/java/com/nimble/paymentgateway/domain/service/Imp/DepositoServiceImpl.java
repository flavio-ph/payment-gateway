package com.nimble.paymentgateway.domain.service.Imp;

import com.nimble.paymentgateway.api.dto.DepositoRequestDTO;
import com.nimble.paymentgateway.api.dto.DepositoResponseDTO;
// (Imports Omitidos)
import com.nimble.paymentgateway.domain.exception.PagamentoException;
import com.nimble.paymentgateway.domain.exception.ResourceNotFoundException;
import com.nimble.paymentgateway.domain.model.Conta;
import com.nimble.paymentgateway.domain.model.Deposito;
import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.model.enums.StatusDeposito;
import com.nimble.paymentgateway.domain.repository.ContaRepository;
import com.nimble.paymentgateway.domain.repository.DepositoRepository;
import com.nimble.paymentgateway.domain.repository.UsuarioRepository;
import com.nimble.paymentgateway.domain.service.DepositoService;
import com.nimble.paymentgateway.infra.integration.AuthorizerClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepositoServiceImpl implements DepositoService {

    private final DepositoRepository depositoRepository;
    private final ContaRepository contaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthorizerClient authorizerClient;

    @Override
    @Transactional
    public DepositoResponseDTO criarDeposito(DepositoRequestDTO dto, String cpfUsuario) {

        Usuario usuario = usuarioRepository.findByCpf(cpfUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (!authorizerClient.authorize()) {
            throw new PagamentoException("Depósito não autorizado pelo serviço externo.");
        }

        Conta conta = contaRepository.findWithLockByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta do usuário não encontrada."));

        conta.setSaldo(conta.getSaldo().add(dto.valor()));
        contaRepository.save(conta);

        Deposito deposito = new Deposito();
        deposito.setUsuario(usuario);
        deposito.setValor(dto.valor());
        deposito.setStatus(StatusDeposito.APROVADO);
        deposito.setReferenciaExterna(UUID.randomUUID().toString());

        var salvo = depositoRepository.save(deposito);
        return mapToDTO(salvo);
    }

    @Override
    public List<DepositoResponseDTO> listarDepositos(String cpfUsuario) {
        Usuario usuario = usuarioRepository.findByCpf(cpfUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        return depositoRepository.findByUsuario_Id(usuario.getId())
                .stream().map(this::mapToDTO).toList();
    }

    @Override
    public DepositoResponseDTO registrarDeposito(DepositoRequestDTO dto) {
        return null;
    }

    private DepositoResponseDTO mapToDTO(Deposito d) {
        return new DepositoResponseDTO(
                d.getId(),
                d.getUsuario().getId(),
                d.getValor(),
                d.getStatus(),
                d.getReferenciaExterna(),
                d.getDataCriacao()
        );
    }
}