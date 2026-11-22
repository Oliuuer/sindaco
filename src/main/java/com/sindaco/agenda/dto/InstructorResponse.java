package com.sindaco.agenda.dto;

/** Datos de salida de un instructor (CU04). */
public record InstructorResponse(
        Long id,
        String nombre,
        String especialidad,
        String telefono,
        Boolean disponible) {
}
