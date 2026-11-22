package com.sindaco.matricula.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sindaco.matricula.dto.ComprobanteResponse;
import com.sindaco.matricula.dto.PagoRequest;
import com.sindaco.matricula.dto.PagoResponse;
import com.sindaco.matricula.service.PagoService;

import jakarta.validation.Valid;

/** Controlador delgado: recibe la petición, delega en el servicio y devuelve la respuesta. */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService service;

    public PagoController(PagoService service) {
        this.service = service;
    }

    @GetMapping
    public List<PagoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public PagoResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @GetMapping("/{id}/comprobante")
    public ComprobanteResponse comprobante(@PathVariable Long id) {
        return service.comprobante(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PagoResponse crear(@Valid @RequestBody PagoRequest request) {
        return service.crear(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
