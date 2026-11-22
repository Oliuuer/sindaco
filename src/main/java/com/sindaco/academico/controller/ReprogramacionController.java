package com.sindaco.academico.controller;

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

import com.sindaco.academico.dto.ReprogramacionRequest;
import com.sindaco.academico.dto.ReprogramacionResponse;
import com.sindaco.academico.service.ReprogramacionService;

import jakarta.validation.Valid;

/** Controlador delgado de reprogramaciones (CU09). */
@RestController
@RequestMapping("/api/reprogramaciones")
public class ReprogramacionController {

    private final ReprogramacionService service;

    public ReprogramacionController(ReprogramacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<ReprogramacionResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public ReprogramacionResponse obtener(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReprogramacionResponse crear(@Valid @RequestBody ReprogramacionRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public ReprogramacionResponse actualizar(@PathVariable Long id,
            @Valid @RequestBody ReprogramacionRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
