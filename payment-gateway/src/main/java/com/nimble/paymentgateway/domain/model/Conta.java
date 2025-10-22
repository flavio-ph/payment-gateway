package com.nimble.paymentgateway.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "conta")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @Version
    private Long versao;

    private LocalDateTime dataAtualizacao;

    @PrePersist
    @PreUpdate
    public void atualizarData() {
        this.dataAtualizacao = LocalDateTime.now();
    }

}
