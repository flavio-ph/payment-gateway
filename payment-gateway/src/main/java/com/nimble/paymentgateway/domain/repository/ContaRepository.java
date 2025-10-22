package com.nimble.paymentgateway.domain.repository;

import com.nimble.paymentgateway.domain.model.Conta;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {

    Optional<Conta> findByUsuario_Id(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Conta> findWithLockByUsuario_Id(Long usuarioId);
}
