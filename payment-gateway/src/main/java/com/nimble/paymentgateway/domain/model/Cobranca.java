package com.nimble.paymentgateway.domain.model;

import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
@Table(name = "cobranca")
public class Cobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_origem_id", nullable = false)
    private Usuario usuarioOrigem;

    @Column(nullable = false, length = 11)
    private String cpfDestino;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(length = 255)
    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusCobranca status;

    @Enumerated(EnumType.STRING)
    private MetodoPagamento metodoPagamento;

    private LocalDateTime dataCriacao;
    private LocalDateTime dataPagamento;

    @PrePersist
    public void prePersist() {
        this.dataCriacao = LocalDateTime.now();
        this.status = StatusCobranca.PENDENTE;
    }

}
