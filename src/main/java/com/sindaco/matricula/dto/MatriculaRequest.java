package com.sindaco.matricula.dto;

import com.sindaco.matricula.enums.EstadoMatricula;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** Datos de entrada para registrar o actualizar una matrícula (CU01). */
public record MatriculaRequest(
        @NotNull(message = "El alumno es obligatorio")
        @Positive(message = "El id del alumno debe ser positivo")
        Long alumnoId,

        @NotNull(message = "El paquete es obligatorio")
        @Positive(message = "El id del paquete debe ser positivo")
        Long paqueteId,

        EstadoMatricula estado,

        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "La fecha debe tener el formato yyyy-MM-dd")
        String fecha) {
}
