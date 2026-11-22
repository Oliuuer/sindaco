package com.sindaco.matricula.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos de entrada para registrar o actualizar un paquete (CU08). */
public record PaqueteRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotBlank(message = "La categoría es obligatoria")
        @Size(max = 20, message = "La categoría no puede superar los 20 caracteres")
        String categoria,

        @NotNull(message = "El número de clases es obligatorio")
        @Positive(message = "El número de clases debe ser mayor a 0")
        Integer numeroClases,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        BigDecimal precio) {
}
