package com.sindaco.mantenimiento.dto;

import java.math.BigDecimal;

import com.sindaco.mantenimiento.enums.EstadoMantenimiento;
import com.sindaco.mantenimiento.enums.TipoMantenimiento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Entrada para registrar o actualizar un mantenimiento. La fecha se recibe como String ISO. */
public record MantenimientoRequest(
        @NotNull(message = "El id del vehículo es obligatorio") Long vehiculoId,
        @NotNull(message = "El tipo de mantenimiento es obligatorio") TipoMantenimiento tipo,
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 500, message = "La descripción no puede superar 500 caracteres") String descripcion,
        @Pattern(regexp = "^$|^\\d{4}-\\d{2}-\\d{2}$", message = "La fecha debe tener formato yyyy-MM-dd") String fecha,
        EstadoMantenimiento estado,
        @NotBlank(message = "El proveedor es obligatorio")
        @Size(max = 150, message = "El proveedor no puede superar 150 caracteres") String proveedor,
        @DecimalMin(value = "0.0", message = "El costo no puede ser negativo")
        @Digits(integer = 10, fraction = 2, message = "El costo debe tener como máximo 10 enteros y 2 decimales")
        BigDecimal costo) {
}
