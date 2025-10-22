package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.domain.exception.BusinessValidationException;
import com.nimble.paymentgateway.domain.model.Cobranca;
import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import com.nimble.paymentgateway.domain.repository.CobrancaRepository;
import com.nimble.paymentgateway.domain.repository.UsuarioRepository;
import com.nimble.paymentgateway.domain.service.Imp.CobrancaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CobrancaServiceImplTest {

    @Mock
    private CobrancaRepository cobrancaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PagamentoService pagamentoService;

    @InjectMocks
    private CobrancaServiceImpl cobrancaService;

    private Usuario solicitante;
    private Usuario outroUsuario;
    private Cobranca cobrancaPaga;
    private Cobranca cobrancaPendente;

    @BeforeEach
    void setUp() {
        solicitante = Usuario.builder().id(1L).cpf("11122233344").build();
        outroUsuario = Usuario.builder().id(2L).cpf("55566677788").build();

        cobrancaPaga = Cobranca.builder()
                .id(1L)
                .usuarioOrigem(solicitante)
                .cpfDestino("55566677788")
                .valor(BigDecimal.TEN)
                .status(StatusCobranca.PAGA)
                .build();

        cobrancaPendente = Cobranca.builder()
                .id(2L)
                .usuarioOrigem(solicitante)
                .cpfDestino("55566677788")
                .valor(BigDecimal.TEN)
                .status(StatusCobranca.PENDENTE)
                .build();
    }

    @Test
    @DisplayName("Deve cancelar cobrança PAGA (chamar estorno)")
    void cancelarCobranca_QuandoPAGA_ChamaPagamentoService() {
        // Arrange
        when(cobrancaRepository.findById(1L)).thenReturn(Optional.of(cobrancaPaga));
        when(usuarioRepository.findByCpf(solicitante.getCpf())).thenReturn(Optional.of(solicitante));
        doNothing().when(pagamentoService).cancelarPagamento(1L);

        cobrancaService.cancelarCobranca(1L, solicitante.getCpf());

        verify(pagamentoService, times(1)).cancelarPagamento(1L);
        verify(cobrancaRepository, never()).save(any(Cobranca.class));
    }

    @Test
    @DisplayName("Deve cancelar cobrança PENDENTE (apenas mudar status)")
    void cancelarCobranca_QuandoPENDENTE_ApenasAtualizaStatus() {
        when(cobrancaRepository.findById(2L)).thenReturn(Optional.of(cobrancaPendente));
        when(usuarioRepository.findByCpf(solicitante.getCpf())).thenReturn(Optional.of(solicitante));

        cobrancaService.cancelarCobranca(2L, solicitante.getCpf());

        verify(pagamentoService, never()).cancelarPagamento(anyLong());
        assertEquals(StatusCobranca.CANCELADA, cobrancaPendente.getStatus());
        verify(cobrancaRepository, times(1)).save(cobrancaPendente);
    }

    @Test
    @DisplayName("Deve falhar ao cancelar cobrança de outro usuário")
    void cancelarCobranca_NaoAutorizado_LancaExcecao() {
        when(cobrancaRepository.findById(1L)).thenReturn(Optional.of(cobrancaPaga));
        when(usuarioRepository.findByCpf(outroUsuario.getCpf())).thenReturn(Optional.of(outroUsuario));
        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            cobrancaService.cancelarCobranca(1L, outroUsuario.getCpf());
        });

        assertEquals("Apenas o criador da cobrança pode cancelá-la.", exception.getMessage());
        verify(pagamentoService, never()).cancelarPagamento(anyLong());
        verify(cobrancaRepository, never()).save(any());
    }

}