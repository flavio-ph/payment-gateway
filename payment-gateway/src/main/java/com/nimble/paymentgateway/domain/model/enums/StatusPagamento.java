package com.nimble.paymentgateway.domain.model.enums;

public enum StatusPagamento {

    APROVADO("Pagamento aprovado"),
    REJEITADO("Pagamento rejeitado"),
    ESTORNADO("Pagamento estornado");

    private final String descricao;

    StatusPagamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
