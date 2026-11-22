package com.sindaco.agenda.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.sindaco.agenda.enums.EstadoClase;

/** Datos de salida de una clase, incluidos los nombres de las entidades relacionadas (CU03). */
public record ClaseResponse(
        Long id,
        String nombre,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        Long alumnoId,
        Long instructorId,
        Long vehiculoId,
        String alumnoNombre,
        String instructorNombre,
        String vehiculoPlaca,
        EstadoClase estado) {
}
