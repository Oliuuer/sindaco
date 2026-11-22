package com.sindaco.matricula.promocion.controller;

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

import com.sindaco.matricula.promocion.dto.PromocionRequest;
import com.sindaco.matricula.promocion.dto.PromocionResponse;
import com.sindaco.matricula.promocion.service.PromocionService;

import jakarta.validation.Valid;

/** Controlador delgado de promociones: delega las operaciones al servicio. */
@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    private final PromocionService service;

    public PromocionController(PromocionService service) {
        this.service = service;
    }

    @GetMapping
    public List<PromocionResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PromocionResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PromocionResponse crear(@Valid @RequestBody PromocionRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public PromocionResponse actualizar(@PathVariable Long id, @Valid @RequestBody PromocionRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/codigo/{codigo}")
    public PromocionResponse buscarVigentePorCodigo(@PathVariable String codigo) {
        return service.buscarVigentePorCodigo(codigo);
    }
}
