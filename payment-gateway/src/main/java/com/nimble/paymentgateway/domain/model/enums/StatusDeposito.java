package com.nimble.paymentgateway.domain.model.enums;

public enum StatusDeposito {

    PENDENTE("Depósito pendente"),
    APROVADO("Depósito aprovado"),
    REJEITADO("Depósito rejeitado");

    private final String descricao;

    StatusDeposito(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}