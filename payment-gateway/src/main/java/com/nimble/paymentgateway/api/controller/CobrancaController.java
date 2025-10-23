package com.nimble.paymentgateway.api.controller;

import com.nimble.paymentgateway.api.dto.CobrancaRequestDTO;
import com.nimble.paymentgateway.api.dto.CobrancaResponseDTO;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca; 
import com.nimble.paymentgateway.domain.service.CobrancaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List; 

@RestController
@RequestMapping("/cobrancas")
public class CobrancaController {

    private final CobrancaService cobrancaService;

    public CobrancaController(CobrancaService cobrancaService) {
        this.cobrancaService = cobrancaService;
    }

    @PostMapping
    public ResponseEntity<CobrancaResponseDTO> criar(
            @Valid @RequestBody CobrancaRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String cpfOrigem = userDetails.getUsername();
        return ResponseEntity.ok(cobrancaService.criarCobranca(dto, cpfOrigem));

        
    }

    @GetMapping("/{id}")
    public ResponseEntity<CobrancaResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(cobrancaService.buscarPorId(id));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        String cpfSolicitante = userDetails.getUsername();
        cobrancaService.cancelarCobranca(id, cpfSolicitante);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/sent")
    public ResponseEntity<List<CobrancaResponseDTO>> listarEnviadas(
            @RequestParam("status") StatusCobranca status,
            @AuthenticationPrincipal UserDetails userDetails) {

        String cpfUsuario = userDetails.getUsername();
        List<CobrancaResponseDTO> cobrancas = cobrancaService.listarCobrancasPorStatus(cpfUsuario, status, true);
        return ResponseEntity.ok(cobrancas);
    }

    @GetMapping("/received")
    public ResponseEntity<List<CobrancaResponseDTO>> listarRecebidas(
            @RequestParam("status") StatusCobranca status,
            @AuthenticationPrincipal UserDetails userDetails) {

        String cpfUsuario = userDetails.getUsername();
        List<CobrancaResponseDTO> cobrancas = cobrancaService.listarCobrancasPorStatus(cpfUsuario, status, false);
        return ResponseEntity.ok(cobrancas);
    }

}