package com.nimble.paymentgateway.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "O nome é obrigatório.")
        String nome,

        @NotBlank(message = "O CPF é obrigatório.")
        @Size(min = 11, max = 11, message = "O CPF deve conter 11 dígitos.")
        String cpf,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "Formato de e-mail inválido.")
        String email,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
        String senha
) {}