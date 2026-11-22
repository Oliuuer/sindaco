package com.sindaco.matricula.promocion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Datos de entrada para crear o actualizar una promoción. */
public record PromocionRequest(
        @NotBlank(message = "El código es obligatorio")
        @Size(max = 30, message = "El código no puede superar los 30 caracteres")
        String codigo,

        @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
        String descripcion,

        @NotNull(message = "El porcentaje de descuento es obligatorio")
        @DecimalMin(value = "0.01", message = "El porcentaje de descuento debe ser al menos 0.01")
        @DecimalMax(value = "100", message = "El porcentaje de descuento no puede superar 100")
        BigDecimal porcentajeDescuento,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDate fechaFin,

        Boolean vigente) {
}
