package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.api.dto.CobrancaRequestDTO;
import com.nimble.paymentgateway.api.dto.CobrancaResponseDTO;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CobrancaService {

    CobrancaResponseDTO criarCobranca(CobrancaRequestDTO dto, String cpfOrigem);

    CobrancaResponseDTO buscarPorId(Long id);

    void cancelarCobranca(Long id, String cpfSolicitante);

    List<CobrancaResponseDTO> listarCobrancasPorStatus(String cpfUsuario, StatusCobranca status, boolean enviadas);
}