package com.sindaco.academico.controller;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sindaco.academico.dto.HistorialAlumnoResponse;
import com.sindaco.academico.service.HistorialService;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.seguridad.entity.Usuario;
import com.sindaco.seguridad.repository.UsuarioRepository;

/** Controlador delgado del historial académico y financiero (CU10). */
@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    private final HistorialService service;
    private final UsuarioRepository usuarioRepository;

    public HistorialController(HistorialService service, UsuarioRepository usuarioRepository) {
        this.service = service;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/alumnos/{alumnoId}")
    public HistorialAlumnoResponse porAlumno(@PathVariable Long alumnoId) {
        return service.porAlumno(alumnoId);
    }

    /** El alumno solo puede consultar su propio historial: "Validar acceso del usuario" (CU10). */
    @GetMapping("/mi")
    public HistorialAlumnoResponse mi(Principal principal) {
        if (principal == null) {
            throw new ReglaNegocioException("Debe iniciar sesión para consultar su historial");
        }
        Usuario usuario = usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", null));
        if (usuario.getAlumnoId() == null) {
            throw new ReglaNegocioException("El usuario no tiene un alumno vinculado a su cuenta");
        }
        return service.porAlumno(usuario.getAlumnoId());
    }
}
