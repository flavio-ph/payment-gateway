package com.nimble.paymentgateway.api.dto;

import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import com.nimble.paymentgateway.domain.model.enums.StatusPagamento;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(
        Long id,
        Long idCobranca,
        Long idUsuarioPagador,
        BigDecimal valor,
        MetodoPagamento metodo,
        StatusPagamento status,
        String referenciaExterna,
        LocalDateTime dataCriacao
) {}