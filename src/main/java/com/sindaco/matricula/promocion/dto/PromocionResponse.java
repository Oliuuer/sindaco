package com.sindaco.matricula.promocion.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de salida de una promoción, incluyendo si está activa en la fecha actual. */
public record PromocionResponse(
        Long id,
        String codigo,
        String descripcion,
        BigDecimal porcentajeDescuento,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean vigente,
        boolean activaHoy) {
}
