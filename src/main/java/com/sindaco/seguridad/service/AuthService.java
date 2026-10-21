package com.sindaco.seguridad.service;

import com.sindaco.seguridad.dto.LoginRequest;
import com.sindaco.seguridad.dto.LoginResponse;
import com.sindaco.seguridad.entity.Rol;
import com.sindaco.seguridad.entity.Usuario;
import com.sindaco.seguridad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public LoginResponse login(LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(request.getUsername());

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        if (!usuario.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return new LoginResponse(usuario.getUsername(), usuario.getRol().name(), "token-demo");
    }

    public void crearUsuario(String username, String password, Rol rol) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            return;
        }

        usuarioRepository.save(new Usuario(username, password, rol));
    }
}
