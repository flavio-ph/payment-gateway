package com.nimble.paymentgateway.domain.service.Imp;

import com.nimble.paymentgateway.domain.exception.ResourceNotFoundException;
import com.nimble.paymentgateway.domain.model.Conta;
import com.nimble.paymentgateway.domain.repository.ContaRepository;
import com.nimble.paymentgateway.domain.service.ContaService;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
@Data
@Service
@RequiredArgsConstructor
public class ContaServiceImpl implements ContaService {

    private final ContaRepository contaRepository;

    @Override
    @Transactional
    public Conta criarContaParaUsuario(Long usuarioId) {

        Conta conta = new Conta();

        conta.setSaldo(BigDecimal.ZERO);
        return contaRepository.save(conta);
    }

    @Override
    public Conta buscarPorUsuario(Long usuarioId) {
        return contaRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada para este usuário."));
    }

    @Override
    @Transactional
    public void atualizarSaldo(Long usuarioId, BigDecimal novoSaldo) {
        Conta conta = contaRepository.findWithLockByUsuario_Id(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada para este usuário."));
        conta.setSaldo(novoSaldo);
        contaRepository.save(conta);
    }
}