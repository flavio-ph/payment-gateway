package com.nimble.paymentgateway.api.dto;

import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PagamentoRequestDTO(

        @NotNull(message = "O ID da cobrança é obrigatório.")
        Long idCobranca,

        @NotNull(message = "O método de pagamento é obrigatório.")
        MetodoPagamento metodoPagamento,

        @Valid
        CardDetailsDTO cardDetails
) {}
