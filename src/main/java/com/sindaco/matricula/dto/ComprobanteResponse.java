package com.sindaco.matricula.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Comprobante de un pago con los datos de la matrícula (CU02). */
public record ComprobanteResponse(
        String numero,
        LocalDate fecha,
        String alumno,
        String paquete,
        BigDecimal monto,
        String metodo,
        Long matriculaId) {
}
