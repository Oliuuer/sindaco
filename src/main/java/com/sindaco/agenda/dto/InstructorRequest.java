package com.sindaco.agenda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Datos de entrada para registrar o actualizar un instructor (CU04). */
public record InstructorRequest(

        @NotBlank(message = "El nombre es obligatorio")
        String nombre,

        @NotBlank(message = "La especialidad es obligatoria")
        String especialidad,

        @NotBlank(message = "El teléfono es obligatorio")
        @Pattern(regexp = "^[0-9+()\\s-]{6,20}$", message = "El teléfono no es válido")
        String telefono,

        Boolean disponible) {
}
