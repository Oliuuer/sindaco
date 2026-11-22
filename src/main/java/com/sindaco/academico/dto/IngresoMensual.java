package com.sindaco.academico.dto;

import java.math.BigDecimal;

/** Total de ingresos de un mes en formato yyyy-MM (CU06). */
public record IngresoMensual(String mes, BigDecimal monto) {
}
