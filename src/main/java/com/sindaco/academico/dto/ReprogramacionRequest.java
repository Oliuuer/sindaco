package com.sindaco.academico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Entrada para registrar o actualizar una reprogramación. Fechas y horas en ISO. */
public record ReprogramacionRequest(
        @NotNull(message = "El id del alumno es obligatorio") Long alumnoId,
        @NotNull(message = "El id de la clase es obligatorio") Long claseId,
        @NotBlank(message = "El motivo es obligatorio")
        @Size(max = 500, message = "El motivo no puede superar 500 caracteres") String motivo,
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha debe tener formato yyyy-MM-dd") String fecha,
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "La nueva fecha debe tener formato yyyy-MM-dd") String nuevaFecha,
        @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "La nueva hora de inicio debe tener formato HH:mm") String nuevaHoraInicio,
        @Pattern(regexp = "^$|^([01]\\d|2[0-3]):[0-5]\\d$", message = "La nueva hora de fin debe tener formato HH:mm") String nuevaHoraFin,
        @Pattern(regexp = "^$|^(PENDIENTE|PENDIENTE_REPROGRAMACION|REPROGRAMADA|JUSTIFICADA)$",
                message = "El estado de la reprogramación no es válido") String estado) {
}
