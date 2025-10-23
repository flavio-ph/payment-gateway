package com.nimble.paymentgateway.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimble.paymentgateway.api.dto.AuthRequestDTO;
import com.nimble.paymentgateway.api.dto.AuthResponseDTO;
import com.nimble.paymentgateway.api.dto.RegisterRequestDTO;
import com.nimble.paymentgateway.domain.service.UsuarioService;
import com.nimble.paymentgateway.infra.config.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private Authentication authentication;


    @Test
    @DisplayName("Deve registrar usuário com sucesso")
    void registrar_ComDadosValidos_RetornaCreated() throws Exception {
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "Nome Teste", "11122233344", "teste@email.com", "senha123"
        );



        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated());

        verify(usuarioService).registrarUsuario(
                eq("Nome Teste"), eq("teste@email.com"), eq("11122233344"), eq("senha123")
        );
    }

    @Test
    @DisplayName("Deve autenticar usuário com sucesso e retornar token")
    void login_ComCredenciaisValidas_RetornaOkComToken() throws Exception {
        AuthRequestDTO requestDTO = new AuthRequestDTO("11122233344", "senha123");
        String expectedToken = "mockJWTToken";
        AuthResponseDTO expectedResponse = new AuthResponseDTO(expectedToken);

        when(authentication.getName()).thenReturn("11122233344");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.gerarToken("11122233344")).thenReturn(expectedToken);

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(expectedToken))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).gerarToken("11122233344");
    }

    @Test
    @DisplayName("Deve retornar Unauthorized para login com credenciais inválidas")
    void login_ComCredenciaisInvalidas_RetornaUnauthorized() throws Exception {
        AuthRequestDTO requestDTO = new AuthRequestDTO("11122233344", "senhaErrada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciais inválidas. Verifique seu CPF/e-mail ou senha."));

        verify(jwtTokenProvider, never()).gerarToken(anyString());
    }

    @Test
    @DisplayName("Deve retornar Bad Request ao registrar com dados inválidos (ex: CPF curto)")
    void registrar_ComCPFInvalido_RetornaBadRequest() throws Exception {
        // Arrange
        RegisterRequestDTO requestDTO = new RegisterRequestDTO(
                "Nome Teste", "123", "teste@email.com", "senha123"
        );

        // Act & Assert
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(usuarioService, never()).registrarUsuario(anyString(), anyString(), anyString(), anyString());
    }
}