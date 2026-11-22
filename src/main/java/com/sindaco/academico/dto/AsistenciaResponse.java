package com.sindaco.academico.dto;

import java.time.LocalDate;

/** Salida del contrato de asistencia: incluye los nombres resueltos de clase y alumno. */
public record AsistenciaResponse(
        Long id,
        Long claseId,
        String claseNombre,
        Long alumnoId,
        String alumnoNombre,
        boolean asistio,
        Integer calificacion,
        String observaciones,
        LocalDate fecha) {
}
