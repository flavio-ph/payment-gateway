package com.nimble.paymentgateway.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record DepositoRequestDTO(

        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser positivo.")
        @DecimalMax(value = "1000000.00", message = "O valor máximo para depósito é R$ 1.000.000,00.")
        BigDecimal valor

) {}