package com.sindaco.seguridad.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.comun.exception.ConflictoException;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.seguridad.dto.UsuarioRequest;
import com.sindaco.seguridad.dto.UsuarioResponse;
import com.sindaco.seguridad.entity.Usuario;
import com.sindaco.seguridad.repository.UsuarioRepository;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getUsername(), u.getNombre(), u.getRol().name(),
                        u.getAlumnoId()))
                .toList();
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (repository.findByUsername(request.username()).isPresent()) {
            throw new ConflictoException("Ya existe un usuario con ese nombre: " + request.username());
        }
        Usuario usuario = new Usuario(request.username(), passwordEncoder.encode(request.password()),
                request.nombre(), request.rol(), request.alumnoId());
        Usuario guardado = repository.save(usuario);
        return new UsuarioResponse(guardado.getId(), guardado.getUsername(), guardado.getNombre(),
                guardado.getRol().name(), guardado.getAlumnoId());
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
        repository.delete(usuario);
    }
}
