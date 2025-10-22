package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.api.dto.DepositoRequestDTO;
import com.nimble.paymentgateway.domain.exception.PagamentoException;
import com.nimble.paymentgateway.domain.model.Conta;
import com.nimble.paymentgateway.domain.model.Deposito;
import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.model.enums.StatusDeposito;
import com.nimble.paymentgateway.domain.repository.ContaRepository;
import com.nimble.paymentgateway.domain.repository.DepositoRepository;
import com.nimble.paymentgateway.domain.repository.UsuarioRepository;
import com.nimble.paymentgateway.domain.service.Imp.DepositoServiceImpl;
import com.nimble.paymentgateway.infra.integration.AuthorizerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepositoServiceImplTest {

    @Mock
    private DepositoRepository depositoRepository;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AuthorizerClient authorizerClient;

    @InjectMocks
    private DepositoServiceImpl depositoService;

    private Usuario usuario;
    private Conta conta;
    private DepositoRequestDTO depositoRequest;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder().id(1L).cpf("11122233344").build();
        conta = new Conta();
        conta.setId(1L);
        conta.setUsuario(usuario);
        conta.setSaldo(new BigDecimal("100.00"));

        depositoRequest = new DepositoRequestDTO(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Deve criar depósito com sucesso")
    void criarDeposito_ComSucesso() {
        // Arrange
        when(usuarioRepository.findByCpf("11122233344")).thenReturn(Optional.of(usuario));
        when(authorizerClient.authorize()).thenReturn(true);
        when(contaRepository.findWithLockByUsuario_Id(1L)).thenReturn(Optional.of(conta));

        ArgumentCaptor<Deposito> depositoCaptor = ArgumentCaptor.forClass(Deposito.class);

        depositoService.criarDeposito(depositoRequest, "11122233344");

        assertEquals(new BigDecimal("150.00"), conta.getSaldo()); // 100 + 50
        verify(contaRepository, times(1)).save(conta);

        verify(depositoRepository, times(1)).save(depositoCaptor.capture());
        Deposito depositoSalvo = depositoCaptor.getValue();
        assertEquals(usuario, depositoSalvo.getUsuario());
        assertEquals(new BigDecimal("50.00"), depositoSalvo.getValor());
        assertEquals(StatusDeposito.APROVADO, depositoSalvo.getStatus());
        assertNotNull(depositoSalvo.getReferenciaExterna());

        verify(authorizerClient, times(1)).authorize();
    }

    @Test
    @DisplayName("Deve falhar depósito se autorizador falhar")
    void criarDeposito_NaoAutorizado_LancaExcecao() {
        when(usuarioRepository.findByCpf("11122233344")).thenReturn(Optional.of(usuario));
        when(authorizerClient.authorize()).thenReturn(false);

        PagamentoException exception = assertThrows(PagamentoException.class, () -> {
            depositoService.criarDeposito(depositoRequest, "11122233344");
        });

        assertEquals("Depósito não autorizado pelo serviço externo.", exception.getMessage());

        verify(contaRepository, never()).findWithLockByUsuario_Id(anyLong());
        verify(contaRepository, never()).save(any());
        verify(depositoRepository, never()).save(any());
    }

}
