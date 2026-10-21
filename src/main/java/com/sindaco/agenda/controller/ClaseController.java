package com.sindaco.agenda.controller;

import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;
import com.sindaco.agenda.service.ClaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clases")
public class ClaseController {

    private final ClaseService service;

    public ClaseController(ClaseService service) {
        this.service = service;
    }

    @GetMapping
    public List<ClaseResponse> listar() {
        return service.listar();
    }

    @PostMapping
    public ResponseEntity<ClaseResponse> crear(@RequestBody ClaseRequest request) {
        return ResponseEntity.ok(service.crear(request));
    }
}
