package com.nimble.paymentgateway.domain.model;

import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import com.nimble.paymentgateway.domain.model.enums.StatusPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cobranca_id", nullable = false)
    private Cobranca cobranca;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_pagador_id", nullable = false)
    private Usuario usuarioPagador;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodo;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status;

    private String referenciaExterna;
    private LocalDateTime dataCriacao;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
    }

}