package com.sindaco.matricula.dto;

import com.sindaco.matricula.enums.EstadoMatricula;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos de entrada para registrar o actualizar una matrícula (CU01 y CU08). */
public record MatriculaRequest(
        @NotNull(message = "El alumno es obligatorio")
        @Positive(message = "El id del alumno debe ser positivo")
        Long alumnoId,

        @NotNull(message = "El paquete es obligatorio")
        @Positive(message = "El id del paquete debe ser positivo")
        Long paqueteId,

        EstadoMatricula estado,

        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "La fecha debe tener el formato yyyy-MM-dd")
        String fecha,

        @Size(max = 30, message = "El código de promoción no puede superar los 30 caracteres")
        String codigoPromocion) {
}
