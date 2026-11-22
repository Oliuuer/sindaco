package com.sindaco.academico.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sindaco.academico.dto.HistorialAlumnoResponse;
import com.sindaco.academico.service.HistorialService;

/** Controlador delgado del historial académico y financiero (CU10). */
@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    private final HistorialService service;

    public HistorialController(HistorialService service) {
        this.service = service;
    }

    @GetMapping("/alumnos/{alumnoId}")
    public HistorialAlumnoResponse porAlumno(@PathVariable Long alumnoId) {
        return service.porAlumno(alumnoId);
    }
}
