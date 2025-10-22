package com.nimble.paymentgateway.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper; // Para converter objetos para JSON
import com.nimble.paymentgateway.api.dto.CobrancaRequestDTO;
import com.nimble.paymentgateway.api.dto.CobrancaResponseDTO;
import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import com.nimble.paymentgateway.domain.service.CobrancaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser; // Simula usuário autenticado
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CobrancaController.class)
class CobrancaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CobrancaService cobrancaService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Deve criar cobrança com sucesso")
    @WithMockUser(username = "11122233344")
    void criar_ComDadosValidos_RetornaCreated() throws Exception {
        CobrancaRequestDTO requestDTO = new CobrancaRequestDTO(
                "55566677788", new BigDecimal("150.00"), "Teste Cobranca", MetodoPagamento.SALDO
        );
        CobrancaResponseDTO responseDTO = new CobrancaResponseDTO(
                1L, 1L, "55566677788", new BigDecimal("150.00"), "Teste Cobranca",
                MetodoPagamento.SALDO, StatusCobranca.PENDENTE, LocalDateTime.now(), null
        );

        given(cobrancaService.criarCobranca(any(CobrancaRequestDTO.class), eq("11122233344")))
                .willReturn(responseDTO);
        mockMvc.perform(post("/cobrancas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cpfDestino").value("55566677788"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        verify(cobrancaService).criarCobranca(any(CobrancaRequestDTO.class), eq("11122233344"));
    }

    @Test
    @DisplayName("Deve retornar erro ao criar cobrança com valor negativo")
    @WithMockUser(username = "11122233344")
    void criar_ComValorInvalido_RetornaBadRequest() throws Exception {
        // Arrange
        CobrancaRequestDTO requestDTO = new CobrancaRequestDTO(
                "55566677788", new BigDecimal("-10.00"),
                "Valor negativo", MetodoPagamento.SALDO
        );

        // Act & Assert
        mockMvc.perform(post("/cobrancas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve buscar cobrança por ID com sucesso")
    @WithMockUser
    void buscar_ComIdExistente_RetornaCobranca() throws Exception {
        // Arrange
        Long cobrancaId = 5L;
        CobrancaResponseDTO responseDTO = new CobrancaResponseDTO(
                cobrancaId, 1L, "11122233344", new BigDecimal("99.00"), "Detalhe",
                MetodoPagamento.CARTAO, StatusCobranca.PAGA, LocalDateTime.now().minusDays(1), LocalDateTime.now()
        );
        given(cobrancaService.buscarPorId(cobrancaId)).willReturn(responseDTO);

        mockMvc.perform(get("/cobrancas/{id}", cobrancaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cobrancaId))
                .andExpect(jsonPath("$.status").value("PAGA"));

        verify(cobrancaService).buscarPorId(cobrancaId);
    }

    @Test
    @DisplayName("Deve listar cobranças enviadas por status")
    @WithMockUser(username="11122233344")
    void listarEnviadas_ComStatusValido_RetornaLista() throws Exception {
        // Arrange
        StatusCobranca status = StatusCobranca.PENDENTE;
        CobrancaResponseDTO cobranca1 = new CobrancaResponseDTO(1L, 1L, "555", BigDecimal.TEN, null, MetodoPagamento.SALDO, status, LocalDateTime.now(), null);
        List<CobrancaResponseDTO> lista = Collections.singletonList(cobranca1);

        given(cobrancaService.listarCobrancasPorStatus("11122233344", status, true)).willReturn(lista);


        mockMvc.perform(get("/cobrancas/sent")
                        .param("status", status.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("PENDENTE"));

        verify(cobrancaService).listarCobrancasPorStatus("11122233344", status, true);
    }

}
