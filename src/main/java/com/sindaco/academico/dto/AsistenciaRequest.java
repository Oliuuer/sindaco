package com.sindaco.academico.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Entrada para registrar o actualizar la asistencia y evaluación de un alumno. */
public record AsistenciaRequest(
        @NotNull(message = "El id de la clase es obligatorio") Long claseId,
        @NotNull(message = "El campo asistio es obligatorio") Boolean asistio,
        @NotNull(message = "La calificación es obligatoria")
        @Min(value = 0, message = "La calificación debe estar entre 0 y 20")
        @Max(value = 20, message = "La calificación debe estar entre 0 y 20") Integer calificacion,
        @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres") String observaciones) {
}
