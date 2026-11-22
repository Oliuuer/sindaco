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

import com.sindaco.academico.dto.AsistenciaRequest;
import com.sindaco.academico.dto.AsistenciaResponse;
import com.sindaco.academico.service.AsistenciaService;

import jakarta.validation.Valid;

/** Controlador delgado de asistencias y evaluaciones (CU05). */
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService service;

    public AsistenciaController(AsistenciaService service) {
        this.service = service;
    }

    @GetMapping
    public List<AsistenciaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public AsistenciaResponse obtener(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AsistenciaResponse crear(@Valid @RequestBody AsistenciaRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public AsistenciaResponse actualizar(@PathVariable Long id, @Valid @RequestBody AsistenciaRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
