package com.sindaco.academico.dto;

import java.math.BigDecimal;

/** Total de ingresos agrupado por método de pago (CU06). */
public record IngresoPorMetodo(String metodo, BigDecimal monto, long cantidad) {
}
