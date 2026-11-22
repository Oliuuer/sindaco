package com.sindaco.academico.dto;

import java.time.LocalDate;

/** Ítem del historial académico de un alumno (CU10). */
public record ItemAcademico(
        LocalDate fecha,
        String paquete,
        String estado,
        String clase,
        String asistencia,
        Integer calificacion) {
}
