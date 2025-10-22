package com.nimble.paymentgateway.domain.model.enums;

public enum StatusCobranca {

    PENDENTE("Cobrança pendente"),
    PAGA("Cobrança paga"),
    CANCELADA("Cobrança cancelada");

    private final String descricao;

    StatusCobranca(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}