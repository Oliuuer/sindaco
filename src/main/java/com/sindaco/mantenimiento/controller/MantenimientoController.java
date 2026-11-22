package com.sindaco.mantenimiento.controller;

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

import com.sindaco.mantenimiento.dto.MantenimientoRequest;
import com.sindaco.mantenimiento.dto.MantenimientoResponse;
import com.sindaco.mantenimiento.service.MantenimientoService;

import jakarta.validation.Valid;

/** Controlador delgado de mantenimientos (CU07). */
@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoController {

    private final MantenimientoService service;

    public MantenimientoController(MantenimientoService service) {
        this.service = service;
    }

    @GetMapping
    public List<MantenimientoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public MantenimientoResponse obtener(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MantenimientoResponse crear(@Valid @RequestBody MantenimientoRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public MantenimientoResponse actualizar(@PathVariable Long id,
            @Valid @RequestBody MantenimientoRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
