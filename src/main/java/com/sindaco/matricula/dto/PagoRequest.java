package com.sindaco.matricula.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** Datos de entrada para registrar un pago (CU02). */
public record PagoRequest(
        @NotNull(message = "La matrícula es obligatoria")
        @Positive(message = "El id de la matrícula debe ser positivo")
        Long matriculaId,

        @NotNull(message = "El monto es obligatorio")
        @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
        BigDecimal monto,

        @NotBlank(message = "El método de pago es obligatorio")
        @Pattern(regexp = "(?i)(EFECTIVO|YAPE|PLIN|TRANSFERENCIA)",
                message = "El método de pago debe ser EFECTIVO, YAPE, PLIN o TRANSFERENCIA")
        String metodo,

        @Size(max = 50, message = "El comprobante no puede superar los 50 caracteres")
        String comprobante,

        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "La fecha debe tener el formato yyyy-MM-dd")
        String fecha) {
}
