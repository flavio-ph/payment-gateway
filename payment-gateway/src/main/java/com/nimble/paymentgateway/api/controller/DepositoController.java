package com.nimble.paymentgateway.api.controller;

import com.nimble.paymentgateway.api.dto.DepositoRequestDTO;
import com.nimble.paymentgateway.api.dto.DepositoResponseDTO;
import com.nimble.paymentgateway.domain.service.DepositoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/depositos")
public class DepositoController {

    private final DepositoService depositoService;

    public DepositoController(DepositoService depositoService) {
        this.depositoService = depositoService;
    }

    @PostMapping
    public ResponseEntity<DepositoResponseDTO> criar(
            @Valid @RequestBody DepositoRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String cpfUsuario = userDetails.getUsername();
        return ResponseEntity.ok(depositoService.criarDeposito(dto, cpfUsuario));
    }

    @GetMapping
    public ResponseEntity<List<DepositoResponseDTO>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {

        String cpfUsuario = userDetails.getUsername();
        return ResponseEntity.ok(depositoService.listarDepositos(cpfUsuario));
    }
}

