package com.nimble.paymentgateway.api.dto;


import jakarta.validation.constraints.NotBlank;

public record AuthRequestDTO(
        @NotBlank(message = "O campo CPF ou e-mail é obrigatório.")
        String usernameOrCpf,

        @NotBlank(message = "A senha é obrigatória.")
        String senha
) {}