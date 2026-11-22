package com.sindaco.agenda.controller;

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

import com.sindaco.agenda.dto.InstructorRequest;
import com.sindaco.agenda.dto.InstructorResponse;
import com.sindaco.agenda.service.InstructorService;

import jakarta.validation.Valid;

/** Controlador delgado de instructores: solo valida el formato y delega en el servicio. */
@RestController
@RequestMapping("/api/instructores")
public class InstructorController {

    private final InstructorService service;

    public InstructorController(InstructorService service) {
        this.service = service;
    }

    @GetMapping
    public List<InstructorResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public InstructorResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InstructorResponse crear(@Valid @RequestBody InstructorRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public InstructorResponse actualizar(@PathVariable Long id, @Valid @RequestBody InstructorRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
