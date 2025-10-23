package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.api.dto.AuthRequestDTO;
import com.nimble.paymentgateway.api.dto.AuthResponseDTO;
import com.nimble.paymentgateway.api.dto.CobrancaRequestDTO;
import com.nimble.paymentgateway.api.dto.CobrancaResponseDTO;
import com.nimble.paymentgateway.api.dto.PagamentoRequestDTO;
import com.nimble.paymentgateway.api.dto.PagamentoResponseDTO;
import com.nimble.paymentgateway.domain.model.Conta;
import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.model.enums.MetodoPagamento;
import com.nimble.paymentgateway.domain.model.enums.StatusCobranca;
import com.nimble.paymentgateway.domain.model.enums.StatusPagamento;
import com.nimble.paymentgateway.domain.repository.ContaRepository; 
import com.nimble.paymentgateway.domain.repository.UsuarioRepository; 
import com.nimble.paymentgateway.domain.service.UsuarioService; 
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort; 
import org.springframework.http.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class PagamentoIntegrationTest {


    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    private String jwtTokenPagador;
    private String jwtTokenRecebedor;
    private Usuario pagador;
    private Usuario recebedor;
    private Long cobrancaId;


    @BeforeEach
    void setUpDatabaseAndLogin() {


        pagador = ensureUserExists("Pagador Teste", "pagador@test.com", "11122233300", "senha123");
        recebedor = ensureUserExists("Recebedor Teste", "recebedor@test.com", "55566677700", "senha123");


        setupContaSaldo(pagador, new BigDecimal("200.00"));
        setupContaSaldo(recebedor, new BigDecimal("500.00"));

        jwtTokenPagador = loginAndGetToken("11122233300", "senha123");
        jwtTokenRecebedor = loginAndGetToken("55566677700", "senha123");

        cobrancaId = criarCobranca(recebedor.getCpf(), pagador.getCpf(), new BigDecimal("100.00"), jwtTokenRecebedor);

    }

    private Usuario ensureUserExists(String nome, String email, String cpf, String senha) {
        return usuarioRepository.findByCpf(cpf).orElseGet(() ->
                usuarioService.registrarUsuario(nome, email, cpf, senha)
        );
    }

    private void setupContaSaldo(Usuario usuario, BigDecimal saldo) {
        Conta conta = contaRepository.findByUsuario_Id(usuario.getId())
                .orElseThrow(() -> new IllegalStateException("Conta não encontrada para usuário: " + usuario.getId()));
        conta.setSaldo(saldo);
        contaRepository.save(conta);
    }


    private String loginAndGetToken(String usernameOrCpf, String password) {
        String loginUrl = "http://localhost:" + port + "/auth/login";
        AuthRequestDTO loginRequest = new AuthRequestDTO(usernameOrCpf, password);
        ResponseEntity<AuthResponseDTO> response = restTemplate.postForEntity(loginUrl, loginRequest, AuthResponseDTO.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().accessToken());
        return response.getBody().accessToken();
    }

    private Long criarCobranca(String cpfOrigem, String cpfDestino, BigDecimal valor, String tokenOriginador) {
        String cobrancaUrl = "http://localhost:" + port + "/cobrancas";
        CobrancaRequestDTO request = new CobrancaRequestDTO(cpfDestino, valor, "Cobranca Teste Integracao", MetodoPagamento.SALDO);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenOriginador);
        HttpEntity<CobrancaRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<CobrancaResponseDTO> response = restTemplate.exchange(cobrancaUrl, HttpMethod.POST, entity, CobrancaResponseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().id());
        assertEquals(StatusCobranca.PENDENTE, response.getBody().status());
        return response.getBody().id();
    }

    @Test
    @DisplayName("Deve processar pagamento com saldo com sucesso via API")
    void processarPagamentoSaldo_Integration() {
        // Arrange
        String pagamentoUrl = "http://localhost:" + port + "/pagamentos";
        PagamentoRequestDTO request = new PagamentoRequestDTO(cobrancaId, MetodoPagamento.SALDO, null);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtTokenPagador);
        HttpEntity<PagamentoRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<PagamentoResponseDTO> response = restTemplate.exchange(pagamentoUrl, HttpMethod.POST, entity, PagamentoResponseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        PagamentoResponseDTO responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(cobrancaId, responseBody.idCobranca());
        assertEquals(pagador.getId(), responseBody.idUsuarioPagador());
        assertEquals(MetodoPagamento.SALDO, responseBody.metodo());
        assertEquals(StatusPagamento.APROVADO, responseBody.status());
        assertEquals(0, new BigDecimal("100.00").compareTo(responseBody.valor()));

        Conta contaPagadorAtualizada = contaRepository.findByUsuario_Id(pagador.getId()).orElseThrow();
        Conta contaRecebedorAtualizada = contaRepository.findByUsuario_Id(recebedor.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("100.00").compareTo(contaPagadorAtualizada.getSaldo()), "Saldo do pagador incorreto"); // 200 - 100
        assertEquals(0, new BigDecimal("600.00").compareTo(contaRecebedorAtualizada.getSaldo()), "Saldo do recebedor incorreto"); // 500 + 100


    }

    @Test
    @DisplayName("Deve falhar pagamento com saldo insuficiente via API")
    void processarPagamentoSaldoInsuficiente_Integration() {
        setupContaSaldo(pagador, new BigDecimal("50.00"));

        String pagamentoUrl = "http://localhost:" + port + "/pagamentos";
        PagamentoRequestDTO request = new PagamentoRequestDTO(cobrancaId, MetodoPagamento.SALDO, null);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtTokenPagador);
        HttpEntity<PagamentoRequestDTO> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Object> response = restTemplate.exchange(pagamentoUrl, HttpMethod.POST, entity, Object.class); // Expecting error response

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());


        Conta contaPagadorNaoModificada = contaRepository.findByUsuario_Id(pagador.getId()).orElseThrow();
        Conta contaRecebedorNaoModificada = contaRepository.findByUsuario_Id(recebedor.getId()).orElseThrow();

        assertEquals(0, new BigDecimal("50.00").compareTo(contaPagadorNaoModificada.getSaldo()), "Saldo do pagador deveria permanecer 50.00");
        assertEquals(0, new BigDecimal("500.00").compareTo(contaRecebedorNaoModificada.getSaldo()), "Saldo do recebedor deveria permanecer 500.00");
    }

}