package com.sindaco.academico.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.sindaco.academico.enums.EstadoReprogramacion;

/** Salida del contrato de reprogramación: incluye nombres resueltos de alumno y clase. */
public record ReprogramacionResponse(
        Long id,
        Long alumnoId,
        String alumnoNombre,
        Long claseId,
        String claseNombre,
        String motivo,
        LocalDate fecha,
        LocalDate nuevaFecha,
        LocalTime nuevaHoraInicio,
        LocalTime nuevaHoraFin,
        EstadoReprogramacion estado) {
}
