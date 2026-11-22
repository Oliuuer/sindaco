package com.sindaco.agenda.controller;

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

import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;
import com.sindaco.agenda.service.ClaseService;
import com.sindaco.seguridad.service.UsuarioActualService;

import jakarta.validation.Valid;

/** Controlador delgado de clases: solo valida el formato y delega en el servicio. */
@RestController
@RequestMapping("/api/clases")
public class ClaseController {

    private final ClaseService service;
    private final UsuarioActualService usuarioActual;

    public ClaseController(ClaseService service, UsuarioActualService usuarioActual) {
        this.service = service;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping
    public List<ClaseResponse> listar() {
        return service.listar();
    }

    /** Portal del alumno: sus horarios de clase, sin exponer los de otros alumnos. */
    @GetMapping("/mi")
    public List<ClaseResponse> mi(Principal principal) {
        return service.listarPorAlumno(usuarioActual.alumnoId(principal));
    }

    @GetMapping("/{id}")
    public ClaseResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaseResponse crear(@Valid @RequestBody ClaseRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public ClaseResponse actualizar(@PathVariable Long id, @Valid @RequestBody ClaseRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
