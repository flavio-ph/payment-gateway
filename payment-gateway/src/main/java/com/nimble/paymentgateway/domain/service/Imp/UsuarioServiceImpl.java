package com.nimble.paymentgateway.domain.service.Imp;

import com.nimble.paymentgateway.domain.exception.BusinessValidationException;
import com.nimble.paymentgateway.domain.exception.ResourceNotFoundException;
import com.nimble.paymentgateway.domain.model.Conta;
import com.nimble.paymentgateway.domain.model.Usuario;
import com.nimble.paymentgateway.domain.repository.ContaRepository;
import com.nimble.paymentgateway.domain.repository.UsuarioRepository;
import com.nimble.paymentgateway.domain.service.UsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ContaRepository contaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    @Override
    public Usuario buscarPorCpf(String cpf) {
        return usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    @Override
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
    }

    @Override
    @Transactional
    public Usuario registrarUsuario(String nome, String email, String cpf, String senha) {
        if (usuarioRepository.existsByCpf(cpf))
            throw new BusinessValidationException("Já existe um usuário com este CPF.");

        if (usuarioRepository.existsByEmail(email))
            throw new BusinessValidationException("Já existe um usuário com este e-mail.");

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setCpf(cpf);
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(senha));

        Usuario novoUsuario = usuarioRepository.save(usuario);

        Conta novaConta = new Conta();
        novaConta.setUsuario(novoUsuario);
        novaConta.setSaldo(BigDecimal.ZERO);
        contaRepository.save(novaConta);

        return novoUsuario;
    }
}