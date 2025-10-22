package com.nimble.paymentgateway.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CardDetailsDTO(
        @NotBlank(message = "O número do cartão é obrigatório.")
        String numeroCartao,

        @NotBlank(message = "A data de expiração é obrigatória.")
        @Pattern(regexp = "^(0[1-9]|1[0-2])/([0-9]{2})$", message = "Data de expiração deve estar no formato MM/AA")
        String dataExpiracao,

        @NotBlank(message = "O CVV é obrigatório.")
        @Size(min = 3, max = 4, message = "CVV deve ter 3 ou 4 dígitos.")
        String cvv
) {}
