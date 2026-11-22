package com.sindaco.mantenimiento.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sindaco.mantenimiento.enums.EstadoMantenimiento;
import com.sindaco.mantenimiento.enums.TipoMantenimiento;

/** Salida del contrato de mantenimiento: incluye la placa resuelta del vehículo. */
public record MantenimientoResponse(
        Long id,
        Long vehiculoId,
        String vehiculoPlaca,
        TipoMantenimiento tipo,
        String descripcion,
        LocalDate fecha,
        EstadoMantenimiento estado,
        String proveedor,
        BigDecimal costo) {
}
