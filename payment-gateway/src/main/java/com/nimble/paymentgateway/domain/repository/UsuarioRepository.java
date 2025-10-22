package com.nimble.paymentgateway.domain.repository;

import com.nimble.paymentgateway.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCpf(String cpf);

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCpfOrEmail(String cpf, String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);
}