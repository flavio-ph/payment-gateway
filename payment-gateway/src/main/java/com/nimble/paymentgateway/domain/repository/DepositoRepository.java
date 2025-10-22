package com.nimble.paymentgateway.domain.repository;

import com.nimble.paymentgateway.domain.model.Deposito;
import com.nimble.paymentgateway.domain.model.enums.StatusDeposito;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepositoRepository extends JpaRepository<Deposito, Long> {

    List<Deposito> findByUsuario_Id(Long idUsuario);

    List<Deposito> findByStatus(StatusDeposito status);

    Optional<Deposito> findByReferenciaExterna(String referenciaExterna);
}