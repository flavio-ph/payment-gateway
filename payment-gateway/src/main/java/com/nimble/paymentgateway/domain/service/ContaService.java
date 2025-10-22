package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.domain.model.Conta;

import java.math.BigDecimal;

public interface ContaService {

    Conta criarContaParaUsuario(Long usuarioId);
    Conta buscarPorUsuario(Long usuarioId);
    void atualizarSaldo(Long usuarioId, BigDecimal novoSaldo);

}