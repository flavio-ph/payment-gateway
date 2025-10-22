package com.nimble.paymentgateway.api.dto;

import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CobrancaRequestDTO(


        @NotBlank(message = "O CPF de destino é obrigatório.")
        @Size(min = 11, max = 11, message = "O CPF deve conter 11 dígitos.")
        String cpfDestino,

        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser positivo.")
        @DecimalMax(value = "1000000.00", message = "O valor máximo para cobrança é R$ 1.000.000,00.")
        BigDecimal valor,

        String descricao,

        @NotNull(message = "O método de pagamento é obrigatório.")
        MetodoPagamento metodoPagamento
) {}