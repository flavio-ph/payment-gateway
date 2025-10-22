package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.api.dto.DepositoRequestDTO;
import com.nimble.paymentgateway.api.dto.DepositoResponseDTO;

import java.util.List;

public interface DepositoService {

    DepositoResponseDTO registrarDeposito(DepositoRequestDTO dto); // Antigo
    DepositoResponseDTO criarDeposito(DepositoRequestDTO dto, String cpfUsuario);
    List<DepositoResponseDTO> listarDepositos(String cpfUsuario);

}
