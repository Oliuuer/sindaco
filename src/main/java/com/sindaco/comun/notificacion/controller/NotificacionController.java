package com.sindaco.comun.notificacion.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sindaco.comun.notificacion.dto.NotificacionRequest;
import com.sindaco.comun.notificacion.dto.NotificacionResponse;
import com.sindaco.comun.notificacion.service.NotificacionService;

import jakarta.validation.Valid;

/** Controlador delgado de notificaciones del sistema. */
@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService service;

    public NotificacionController(NotificacionService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificacionResponse> listar() {
        return service.listar();
    }

    @GetMapping("/rol/{rol}")
    public List<NotificacionResponse> listarPorRol(@PathVariable String rol) {
        return service.listarPorRol(rol);
    }

    @GetMapping("/alumno/{alumnoId}")
    public List<NotificacionResponse> listarPorAlumno(@PathVariable Long alumnoId) {
        return service.listarPorAlumno(alumnoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificacionResponse crear(@Valid @RequestBody NotificacionRequest request) {
        return service.crear(request);
    }

    @PatchMapping("/{id}/leida")
    public void marcarLeida(@PathVariable Long id) {
        service.marcarLeida(id);
    }
}
