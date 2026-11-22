package com.sindaco.academico.controller;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sindaco.academico.service.ReporteService;
import com.sindaco.comun.RangoFechas;

/** Controlador delgado del reporte de ingresos (CU06). */
@RestController
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    /**
     * Devuelve el reporte en JSON por defecto o un archivo CSV cuando
     * <code>formato=csv</code> (extensión "Exportar reporte" de CU06).
     */
    @GetMapping("/api/reportes/ingresos")
    public ResponseEntity<Object> ingresos(
            @RequestParam(name = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> desde,
            @RequestParam(name = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> hasta,
            @RequestParam(name = "formato", required = false, defaultValue = "json") String formato) {

        LocalDate inicio = RangoFechas.inicio(desde.orElse(null));
        LocalDate fin = RangoFechas.fin(hasta.orElse(null));

        if (formato != null && "csv".equalsIgnoreCase(formato.trim())) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"reporte-ingresos.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                    .body(service.exportarCsv(inicio, fin));
        }
        return ResponseEntity.ok(service.ingresos(inicio, fin));
    }
}
