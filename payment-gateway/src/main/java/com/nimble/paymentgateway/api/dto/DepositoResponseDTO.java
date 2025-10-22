package com.nimble.paymentgateway.api.dto;

import com.nimble.paymentgateway.domain.model.enums.StatusDeposito;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositoResponseDTO(

        Long id,
        Long idUsuario,
        BigDecimal valor,
        StatusDeposito status,
        String referenciaExterna,
        LocalDateTime dataCriacao
) {}