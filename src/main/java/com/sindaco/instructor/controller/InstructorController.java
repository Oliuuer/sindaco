package com.sindaco.instructor.controller;

import com.sindaco.instructor.dto.InstructorRequest;
import com.sindaco.instructor.dto.InstructorResponse;
import com.sindaco.instructor.service.InstructorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<InstructorResponse> crear(@RequestBody InstructorRequest request) {
        return ResponseEntity.ok(service.crear(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
