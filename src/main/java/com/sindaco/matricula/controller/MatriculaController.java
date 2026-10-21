package com.sindaco.matricula.controller;

import com.sindaco.matricula.dto.MatriculaRequest;
import com.sindaco.matricula.dto.MatriculaResponse;
import com.sindaco.matricula.service.MatriculaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService service;

    public MatriculaController(MatriculaService service) {
        this.service = service;
    }

    @GetMapping
    public List<MatriculaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public MatriculaResponse buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<MatriculaResponse> crear(@RequestBody MatriculaRequest request) {
        return ResponseEntity.ok(service.crear(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
