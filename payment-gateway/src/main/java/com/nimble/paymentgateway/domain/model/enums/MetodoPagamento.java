package com.nimble.paymentgateway.domain.model.enums;

public enum MetodoPagamento {

    SALDO("Pagamento com saldo em conta"),
    CARTAO("Pagamento com cartão de crédito");

    private final String descricao;

    MetodoPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}