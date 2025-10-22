package com.nimble.paymentgateway.domain.service;

import com.nimble.paymentgateway.domain.model.Usuario;
import jakarta.transaction.Transactional;

import java.util.List;

public interface UsuarioService {

    Usuario registrarUsuario(String nome, String email, String cpf, String senha);

    Usuario buscarPorCpf(String cpf);
    Usuario buscarPorEmail(String email);

    Usuario buscarPorId(Long id);
    List<Usuario> listarUsuarios();

}
