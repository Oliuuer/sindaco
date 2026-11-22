package com.sindaco.academico.service;

import java.time.LocalDate;

import com.sindaco.academico.dto.ReporteIngresosResponse;

/** Caso de uso de consolidación y exportación del reporte de ingresos (CU06). */
public interface ReporteService {

    ReporteIngresosResponse ingresos(LocalDate desde, LocalDate hasta);

    /** Genera el CSV del reporte y registra el envío al MTC como notificación. */
    String exportarCsv(LocalDate desde, LocalDate hasta);
}
