package com.sindaco.agenda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para programar o actualizar una clase (CU03).
 * Las fechas y horas llegan como texto ISO y se convierten en el servicio.
 */
public record ClaseRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "La fecha es obligatoria")
        String fecha,

        @NotBlank(message = "La hora de inicio es obligatoria")
        String horaInicio,

        @NotBlank(message = "La hora de fin es obligatoria")
        String horaFin,

        @NotNull(message = "El alumno es obligatorio")
        Long alumnoId,

        @NotNull(message = "El instructor es obligatorio")
        Long instructorId,

        @NotNull(message = "El vehículo es obligatorio")
        Long vehiculoId,

        String estado) {
}
