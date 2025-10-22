package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.api.dto.PagamentoRequestDTO;
import com.nimble.paymentgateway.api.dto.PagamentoResponseDTO;

public interface PagamentoService {
    PagamentoResponseDTO efetuarPagamento(PagamentoRequestDTO dto);

    PagamentoResponseDTO processarPagamento(PagamentoRequestDTO dto, String cpfPagador);
    PagamentoResponseDTO buscarPorId(Long id);
    void cancelarPagamento(Long cobrancaId);
}