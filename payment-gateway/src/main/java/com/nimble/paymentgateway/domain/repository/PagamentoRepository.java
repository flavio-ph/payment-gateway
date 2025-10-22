package com.nimble.paymentgateway.domain.repository;

import com.nimble.paymentgateway.domain.model.Pagamento;
import com.nimble.paymentgateway.domain.model.enums.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByReferenciaExterna(String referenciaExterna);

    List<Pagamento> findByStatus(StatusPagamento status);

    List<Pagamento> findByCobranca_Id(Long idCobranca);
}