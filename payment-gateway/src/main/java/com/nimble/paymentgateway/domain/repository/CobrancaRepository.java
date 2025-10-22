package com.nimble.paymentgateway.domain.repository;

import com.nimble.paymentgateway.api.dto.CobrancaResponseDTO;
import com.nimble.paymentgateway.domain.model.Cobranca;
import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {

    List<Cobranca> findByUsuarioOrigemAndStatus(Usuario usuarioOrigem, StatusCobranca status);

    List<Cobranca> findByCpfDestinoAndStatus(String cpfDestino, StatusCobranca status);
}