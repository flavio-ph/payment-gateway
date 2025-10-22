package com.nimble.paymentgateway.api.controller;

import com.nimble.paymentgateway.api.dto.AuthRequestDTO;
import com.nimble.paymentgateway.api.dto.AuthResponseDTO;
import com.nimble.paymentgateway.api.dto.RegisterRequestDTO;
import com.nimble.paymentgateway.domain.service.UsuarioService;
import com.nimble.paymentgateway.infra.config.JwtTokenProvider;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequestDTO req) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.usernameOrCpf(), req.senha())
            );
            String token = jwtTokenProvider.gerarToken(auth.getName());
            return ResponseEntity.ok(new AuthResponseDTO(token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Credenciais inválidas. Verifique seu CPF/e-mail ou senha.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno ao processar autenticação.");
        }
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void registrar(@Valid @RequestBody RegisterRequestDTO req) {
        usuarioService.registrarUsuario(req.nome(), req.email(), req.cpf(), req.senha());
    }
}