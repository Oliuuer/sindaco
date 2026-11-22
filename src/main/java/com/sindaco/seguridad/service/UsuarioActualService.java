package com.sindaco.seguridad.service;

import java.security.Principal;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.sindaco.seguridad.entity.Rol;
import com.sindaco.seguridad.entity.Usuario;
import com.sindaco.seguridad.repository.UsuarioRepository;

/**
 * Resuelve la identidad del usuario autenticado para acotar los datos del
 * portal del alumno (mis clases, mis pagos y mis matrículas).
 */
@Service
public class UsuarioActualService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario obtener(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Debe iniciar sesión para acceder a esta información");
        }
        return usuarioRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("El usuario autenticado no existe"));
    }

    /** Devuelve el alumno vinculado a la cuenta; el portal del alumno solo muestra ese id. */
    public Long alumnoId(Principal principal) {
        Usuario usuario = obtener(principal);
        if (usuario.getAlumnoId() == null) {
            throw new AccessDeniedException("El usuario no tiene un alumno vinculado a su cuenta");
        }
        return usuario.getAlumnoId();
    }

    public boolean es(Principal principal, Rol rol) {
        return obtener(principal).getRol() == rol;
    }

    /** Verifica que el recurso consultado pertenezca al alumno autenticado. */
    public void verificarPropio(Principal principal, Long alumnoIdDelRecurso) {
        if (es(principal, Rol.ALUMNO) && !alumnoId(principal).equals(alumnoIdDelRecurso)) {
            throw new AccessDeniedException("No tiene permiso para acceder a los datos de otro alumno");
        }
    }
}
