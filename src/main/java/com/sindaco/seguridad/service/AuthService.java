package com.sindaco.seguridad.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.comun.exception.ConflictoException;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.config.JwtService;
import com.sindaco.seguridad.dto.LoginRequest;
import com.sindaco.seguridad.dto.LoginResponse;
import com.sindaco.seguridad.dto.UsuarioResponse;
import com.sindaco.seguridad.entity.Rol;
import com.sindaco.seguridad.entity.Usuario;
import com.sindaco.seguridad.repository.UsuarioRepository;

/** Capa de aplicación: autenticación y consulta del usuario actual. */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow(() -> new ReglaNegocioException("Credenciales inválidas"));

        if (!usuario.isActivo() || !passwordEncoder.matches(request.password(), usuario.getPassword())) {
            throw new ReglaNegocioException("Credenciales inválidas");
        }

        if (request.rol() != null && !request.rol().isBlank()
                && !usuario.getRol().name().equalsIgnoreCase(request.rol())) {
            throw new ReglaNegocioException("El rol seleccionado no corresponde al usuario");
        }

        return new LoginResponse(jwtService.generarToken(usuario), usuario.getUsername(),
                usuario.getNombre(), usuario.getRol().name());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse me(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", null));
        return new UsuarioResponse(usuario.getId(), usuario.getUsername(), usuario.getNombre(), usuario.getRol().name());
    }

    @Transactional
    public Usuario crearUsuario(String username, String password, String nombre, Rol rol) {
        if (usuarioRepository.findByUsername(username).isPresent()) {
            throw new ConflictoException("Ya existe un usuario con ese nombre: " + username);
        }
        Usuario usuario = new Usuario(username, passwordEncoder.encode(password), nombre, rol);
        return usuarioRepository.save(usuario);
    }
}
