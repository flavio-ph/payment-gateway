package com.nimble.paymentgateway.infra.config;

import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrCpfOrEmail) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCpfOrEmail(usernameOrCpfOrEmail, usernameOrCpfOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + usernameOrCpfOrEmail));

        return new User(
                usuario.getCpf(),
                usuario.getSenhaHash(),
                Collections.singletonList(() -> "ROLE_USER")
        );
    }
}