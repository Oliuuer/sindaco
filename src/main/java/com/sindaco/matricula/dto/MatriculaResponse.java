package com.sindaco.matricula.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sindaco.matricula.enums.EstadoMatricula;

/** Datos de una matrícula que se devuelven al frontend (CU01 y CU08). */
public record MatriculaResponse(
        Long id,
        Long alumnoId,
        String alumnoNombre,
        Long paqueteId,
        String paquete,
        EstadoMatricula estado,
        LocalDate fecha,
        BigDecimal precioFinal,
        BigDecimal descuento,
        String codigoPromocion) {
}
