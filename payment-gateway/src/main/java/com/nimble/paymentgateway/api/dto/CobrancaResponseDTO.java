package com.nimble.paymentgateway.api.dto;


import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CobrancaResponseDTO(
        Long id,
        Long idUsuarioOrigem,
        String cpfDestino,
        BigDecimal valor,
        String descricao,
        MetodoPagamento metodoPagamento,
        StatusCobranca status,
        LocalDateTime dataCriacao,
        LocalDateTime dataPagamento
) {}
