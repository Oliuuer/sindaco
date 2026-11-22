package com.sindaco.academico.dto;

import java.math.BigDecimal;
import java.util.List;

/** Reporte consolidado de ingresos en un rango de fechas (CU06). */
public record ReporteIngresosResponse(
        BigDecimal totalIngresos,
        long totalPagos,
        BigDecimal promedioPago,
        List<IngresoPorMetodo> porMetodo,
        List<IngresoMensual> porMes) {
}
