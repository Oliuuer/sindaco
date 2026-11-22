package com.sindaco.academico.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sindaco.academico.dto.ReporteIngresosResponse;
import com.sindaco.academico.service.ReporteService;
import com.sindaco.comun.RangoFechas;

/** Controlador delgado del reporte de ingresos (CU06). */
@RestController
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping("/api/reportes/ingresos")
    public ReporteIngresosResponse ingresos(
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> hasta) {
        return service.ingresos(
                RangoFechas.inicio(desde.orElse(null)),
                RangoFechas.fin(hasta.orElse(null)));
    }
}
