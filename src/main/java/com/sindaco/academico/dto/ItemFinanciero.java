package com.sindaco.academico.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Ítem del historial financiero de un alumno (CU10). */
public record ItemFinanciero(
        LocalDate fecha,
        String estado,
        BigDecimal monto,
        String comprobante) {
}
