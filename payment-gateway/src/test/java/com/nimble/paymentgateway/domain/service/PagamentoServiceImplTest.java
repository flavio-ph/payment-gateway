package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.api.dto.CardDetailsDTO;
import com.nimble.paymentgateway.api.dto.PagamentoRequestDTO;
import com.nimble.paymentgateway.domain.exception.BusinessValidationException;
import com.nimble.paymentgateway.domain.exception.PagamentoException;
import com.nimble.paymentgateway.domain.model.*;
import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import com.nimble.paymentgateway.domain.model.enums.StatusPagamento;
import com.nimble.paymentgateway.domain.repository.*;
import com.nimble.paymentgateway.domain.service.Imp.PagamentoServiceImpl;
import com.nimble.paymentgateway.infra.integration.AuthorizerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagamentoServiceImplTest {

    @Mock
    private PagamentoRepository pagamentoRepository;
    @Mock
    private CobrancaRepository cobrancaRepository;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private AuthorizerClient authorizerClient;
    @Mock
    private PagamentoRequestDTO requestSaldo;
    @Mock
    private PagamentoRequestDTO requestCartao;
    @Mock
    private CardDetailsDTO mockCardDetails;
    @InjectMocks
    private PagamentoServiceImpl pagamentoService;
    private Usuario pagador;
    private Usuario recebedor;
    private Cobranca cobrancaPendenteSaldo;
    private Cobranca cobrancaPendenteCartao;
    private Conta contaPagador;
    private Conta contaRecebedor;


    @BeforeEach
    void setUp() {
        pagador = Usuario.builder().id(1L).cpf("11122233344").nome("Pagador").build();
        recebedor = Usuario.builder().id(2L).cpf("55566677788").nome("Recebedor").build();

        cobrancaPendenteSaldo = Cobranca.builder()
                .id(10L)
                .usuarioOrigem(recebedor)
                .cpfDestino(pagador.getCpf())
                .valor(new BigDecimal("100.00"))
                .status(StatusCobranca.PENDENTE)
                .metodoPagamento(MetodoPagamento.SALDO)
                .build();

        cobrancaPendenteCartao = Cobranca.builder()
                .id(11L)
                .usuarioOrigem(recebedor)
                .cpfDestino(pagador.getCpf())
                .valor(new BigDecimal("50.00"))
                .status(StatusCobranca.PENDENTE)
                .metodoPagamento(MetodoPagamento.CARTAO)
                .build();

        contaPagador = new Conta();
        contaPagador.setId(1L);
        contaPagador.setUsuario(pagador);
        contaPagador.setSaldo(new BigDecimal("200.00"));

        contaRecebedor = new Conta();
        contaRecebedor.setId(2L);
        contaRecebedor.setUsuario(recebedor);
        contaRecebedor.setSaldo(new BigDecimal("500.00"));


        requestSaldo = new PagamentoRequestDTO(cobrancaPendenteSaldo.getId(), MetodoPagamento.SALDO, null);

        mockCardDetails = new CardDetailsDTO("1234567890123456", "12/25", "123");
        requestCartao = new PagamentoRequestDTO(cobrancaPendenteCartao.getId(), MetodoPagamento.CARTAO, mockCardDetails);

    }
    @Test
    @DisplayName("Deve processar pagamento com saldo com sucesso")
    void processarPagamento_ComSaldo_ComSucesso() {
        when(cobrancaRepository.findById(requestSaldo.idCobranca())).thenReturn(Optional.of(cobrancaPendenteSaldo));
        when(usuarioRepository.findByCpf(pagador.getCpf())).thenReturn(Optional.of(pagador));
        when(contaRepository.findWithLockByUsuario_Id(pagador.getId())).thenReturn(Optional.of(contaPagador));
        when(contaRepository.findWithLockByUsuario_Id(recebedor.getId())).thenReturn(Optional.of(contaRecebedor));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento p = invocation.getArgument(0);
            p.setId(1L);
            p.setDataCriacao(LocalDateTime.now());
            return p;
        });

        var response = pagamentoService.processarPagamento(requestSaldo, pagador.getCpf());

        assertNotNull(response);
        assertEquals(StatusPagamento.APROVADO, response.status());
        assertEquals(MetodoPagamento.SALDO, response.metodo());
        assertEquals(cobrancaPendenteSaldo.getId(), response.idCobranca());
        assertEquals(new BigDecimal("100.00"), contaPagador.getSaldo());
        assertEquals(new BigDecimal("600.00"), contaRecebedor.getSaldo());
        assertEquals(StatusCobranca.PAGA, cobrancaPendenteSaldo.getStatus());

        verify(contaRepository, times(2)).save(any(Conta.class));
        verify(cobrancaRepository, times(1)).save(cobrancaPendenteSaldo);
        verify(pagamentoRepository, times(1)).save(any(Pagamento.class));
        verify(authorizerClient, never()).authorize();
    }

    @Test
    @DisplayName("Deve falhar pagamento com saldo insuficiente")
    void processarPagamento_ComSaldo_SaldoInsuficiente() {
        contaPagador.setSaldo(new BigDecimal("50.00"));
        when(cobrancaRepository.findById(requestSaldo.idCobranca())).thenReturn(Optional.of(cobrancaPendenteSaldo));
        when(usuarioRepository.findByCpf(pagador.getCpf())).thenReturn(Optional.of(pagador));
        when(contaRepository.findWithLockByUsuario_Id(pagador.getId())).thenReturn(Optional.of(contaPagador));

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            pagamentoService.processarPagamento(requestSaldo, pagador.getCpf());
        });

        assertEquals("Saldo insuficiente.", exception.getMessage());
        verify(contaRepository, never()).save(any());
        verify(pagamentoRepository, never()).save(any());
        verify(cobrancaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve processar pagamento com cartão com sucesso")
    void processarPagamento_ComCartao_ComSucesso() {
        when(cobrancaRepository.findById(requestCartao.idCobranca())).thenReturn(Optional.of(cobrancaPendenteCartao));
        when(usuarioRepository.findByCpf(pagador.getCpf())).thenReturn(Optional.of(pagador));
        when(authorizerClient.authorize()).thenReturn(true);
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(invocation -> {
            Pagamento p = invocation.getArgument(0);
            p.setId(2L);
            p.setDataCriacao(LocalDateTime.now());
            return p;
        });

        var response = pagamentoService.processarPagamento(requestCartao, pagador.getCpf());

        assertNotNull(response);
        assertEquals(StatusPagamento.APROVADO, response.status());
        assertEquals(MetodoPagamento.CARTAO, response.metodo());
        assertEquals(cobrancaPendenteCartao.getId(), response.idCobranca());
        assertEquals(StatusCobranca.PAGA, cobrancaPendenteCartao.getStatus());
        assertNotNull(response.referenciaExterna());

        verify(authorizerClient, times(1)).authorize();
        verify(cobrancaRepository, times(1)).save(cobrancaPendenteCartao);
        verify(pagamentoRepository, times(1)).save(any(Pagamento.class));
        verify(contaRepository, never()).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve falhar pagamento com cartão não autorizado")
    void processarPagamento_ComCartao_NaoAutorizado() {
        when(cobrancaRepository.findById(requestCartao.idCobranca())).thenReturn(Optional.of(cobrancaPendenteCartao));
        when(usuarioRepository.findByCpf(pagador.getCpf())).thenReturn(Optional.of(pagador));
        when(authorizerClient.authorize()).thenReturn(false);

        PagamentoException exception = assertThrows(PagamentoException.class, () -> {
            pagamentoService.processarPagamento(requestCartao, pagador.getCpf());
        });

        assertEquals("Pagamento com cartão de crédito não autorizado.", exception.getMessage());
        verify(authorizerClient, times(1)).authorize();
        verify(cobrancaRepository, never()).save(any());
        verify(pagamentoRepository, never()).save(any());
        verify(contaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar pagamento se usuário não for o destinatário")
    void processarPagamento_UsuarioNaoDestinatario_LancaExcecao() {
        //
        cobrancaPendenteSaldo.setCpfDestino("99999999999");
        when(cobrancaRepository.findById(requestSaldo.idCobranca())).thenReturn(Optional.of(cobrancaPendenteSaldo));
        when(usuarioRepository.findByCpf(pagador.getCpf())).thenReturn(Optional.of(pagador));

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            pagamentoService.processarPagamento(requestSaldo, pagador.getCpf());
        });

        assertEquals("Este usuário não é o destinatário da cobrança.", exception.getMessage());
        verify(contaRepository, never()).save(any());
        verify(pagamentoRepository, never()).save(any());
    }



}