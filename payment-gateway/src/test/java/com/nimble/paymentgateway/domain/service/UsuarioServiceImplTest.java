package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.domain.exception.BusinessValidationException;
import com.nimble.paymentgateway.domain.model.Conta;
import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.repository.ContaRepository;
import com.nimble.paymentgateway.domain.repository.UsuarioRepository;
import com.nimble.paymentgateway.domain.service.Imp.UsuarioServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    @DisplayName("Deve registrar usuário e criar conta com saldo zero")
    void registrarUsuario_ComSucesso() {
        // Arrange
        String nome = "Usuário Teste";
        String email = "teste@email.com";
        String cpf = "11122233344";
        String senha = "senha123";
        String senhaHash = "hashed_senha";

        when(usuarioRepository.existsByCpf(cpf)).thenReturn(false);
        when(usuarioRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(senha)).thenReturn(senhaHash);

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        ArgumentCaptor<Conta> contaCaptor = ArgumentCaptor.forClass(Conta.class);

        Usuario usuarioRegistrado = usuarioService.registrarUsuario(nome, email, cpf, senha);

        assertNotNull(usuarioRegistrado);
        assertEquals(nome, usuarioRegistrado.getNome());
        assertEquals(cpf, usuarioRegistrado.getCpf());
        assertEquals(senhaHash, usuarioRegistrado.getSenhaHash());
        assertNotNull(usuarioRegistrado.getId());

        verify(contaRepository, times(1)).save(contaCaptor.capture());
        Conta contaSalva = contaCaptor.getValue();
        assertEquals(usuarioRegistrado, contaSalva.getUsuario());
        assertEquals(BigDecimal.ZERO, contaSalva.getSaldo());

        verify(passwordEncoder, times(1)).encode(senha);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve falhar registro com CPF duplicado")
    void registrarUsuario_CpfDuplicado_LancaExcecao() {
        when(usuarioRepository.existsByCpf("11122233344")).thenReturn(true);

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            usuarioService.registrarUsuario("Nome", "email@teste.com", "11122233344", "senha");
        });

        assertEquals("Já existe um usuário com este CPF.", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(contaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar registro com E-mail duplicado")
    void registrarUsuario_EmailDuplicado_LancaExcecao() {
        when(usuarioRepository.existsByCpf(anyString())).thenReturn(false);
        when(usuarioRepository.existsByEmail("email@teste.com")).thenReturn(true);

        BusinessValidationException exception = assertThrows(BusinessValidationException.class, () -> {
            usuarioService.registrarUsuario("Nome", "email@teste.com", "11122233344", "senha");
        });

        assertEquals("Já existe um usuário com este e-mail.", exception.getMessage());
        verify(usuarioRepository, never()).save(any());
        verify(contaRepository, never()).save(any());
    }
}