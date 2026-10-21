package com.sindaco.pago.controller;

import com.sindaco.pago.dto.PagoRequest;
import com.sindaco.pago.dto.PagoResponse;
import com.sindaco.pago.service.PagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    public ResponseEntity<PagoResponse> crear(@RequestBody PagoRequest request) {
        return ResponseEntity.ok(service.crear(request));
    }
}
