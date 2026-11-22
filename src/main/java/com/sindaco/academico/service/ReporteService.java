package com.sindaco.academico.service;

import java.time.LocalDate;

import com.sindaco.academico.dto.ReporteIngresosResponse;

/** Caso de uso de consolidación de reporte de ingresos (CU06). */
public interface ReporteService {

    ReporteIngresosResponse ingresos(LocalDate desde, LocalDate hasta);
}
