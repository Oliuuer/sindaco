package com.sindaco.matricula.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sindaco.matricula.dto.MatriculaRequest;
import com.sindaco.matricula.dto.MatriculaResponse;
import com.sindaco.matricula.service.MatriculaService;
import com.sindaco.seguridad.entity.Rol;
import com.sindaco.seguridad.service.UsuarioActualService;

import jakarta.validation.Valid;

/** Controlador delgado: recibe la petición, delega en el servicio y devuelve la respuesta. */
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService service;
    private final UsuarioActualService usuarioActual;

    public MatriculaController(MatriculaService service, UsuarioActualService usuarioActual) {
        this.service = service;
        this.usuarioActual = usuarioActual;
    }

    /** Los roles operativos ven todas; el alumno únicamente las suyas. */
    @GetMapping
    public List<MatriculaResponse> listar(Principal principal) {
        if (usuarioActual.es(principal, Rol.ALUMNO)) {
            return service.listarPorAlumno(usuarioActual.alumnoId(principal));
        }
        return service.listar();
    }

    /** Portal del alumno: sus propias matrículas. */
    @GetMapping("/mi")
    public List<MatriculaResponse> mi(Principal principal) {
        return service.listarPorAlumno(usuarioActual.alumnoId(principal));
    }

    @GetMapping("/{id}")
    public MatriculaResponse buscarPorId(@PathVariable Long id, Principal principal) {
        MatriculaResponse respuesta = service.buscarPorId(id);
        usuarioActual.verificarPropio(principal, respuesta.alumnoId());
        return respuesta;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse crear(@Valid @RequestBody MatriculaRequest request, Principal principal) {
        if (usuarioActual.es(principal, Rol.ALUMNO)) {
            // El alumno solo puede matricularse a sí mismo y no puede auto-confirmar la matrícula.
            request = new MatriculaRequest(usuarioActual.alumnoId(principal), request.paqueteId(),
                    null, request.fecha(), request.codigoPromocion());
        }
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public MatriculaResponse actualizar(@PathVariable Long id,
                                        @Valid @RequestBody MatriculaRequest request,
                                        Principal principal) {
        MatriculaResponse actual = service.buscarPorId(id);
        usuarioActual.verificarPropio(principal, actual.alumnoId());
        if (usuarioActual.es(principal, Rol.ALUMNO)) {
            request = new MatriculaRequest(actual.alumnoId(), request.paqueteId(),
                    actual.estado(), request.fecha(), request.codigoPromocion());
        }
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, Principal principal) {
        MatriculaResponse actual = service.buscarPorId(id);
        usuarioActual.verificarPropio(principal, actual.alumnoId());
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
