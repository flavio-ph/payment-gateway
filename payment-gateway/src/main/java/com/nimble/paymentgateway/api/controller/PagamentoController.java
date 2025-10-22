package com.nimble.paymentgateway.api.controller;

import com.nimble.paymentgateway.api.dto.PagamentoRequestDTO;
import com.nimble.paymentgateway.api.dto.PagamentoResponseDTO;
import com.nimble.paymentgateway.domain.service.PagamentoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(PagamentoService pagamentoService) {
        this.pagamentoService = pagamentoService;
    }

    @PostMapping
    public ResponseEntity<PagamentoResponseDTO> processar(
            @Valid @RequestBody PagamentoRequestDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String cpfPagador = userDetails.getUsername();
        return ResponseEntity.ok(pagamentoService.processarPagamento(dto, cpfPagador));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagamentoResponseDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(pagamentoService.buscarPorId(id));
    }
}