package com.sindaco.matricula.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de un pago que se devuelven al frontend (CU02). */
public record PagoResponse(
        Long id,
        Long matriculaId,
        String alumnoNombre,
        BigDecimal monto,
        String metodo,
        String comprobante,
        LocalDate fecha) {
}
