package com.sindaco.matricula.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sindaco.matricula.dto.PaqueteRequest;
import com.sindaco.matricula.dto.PaqueteResponse;
import com.sindaco.matricula.service.PaqueteService;

import jakarta.validation.Valid;

/** Controlador delgado: recibe la petición, delega en el servicio y devuelve la respuesta. */
@RestController
@RequestMapping("/api/paquetes")
public class PaqueteController {

    private final PaqueteService service;

    public PaqueteController(PaqueteService service) {
        this.service = service;
    }

    @GetMapping
    public List<PaqueteResponse> listar(@RequestParam(defaultValue = "false") boolean soloVigentes) {
        return service.listar(soloVigentes);
    }

    @GetMapping("/{id}")
    public PaqueteResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaqueteResponse crear(@Valid @RequestBody PaqueteRequest request) {
        return service.crear(request);
    }
}
