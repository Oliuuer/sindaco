package com.sindaco.academico.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.academico.dto.AsistenciaResponse;
import com.sindaco.academico.dto.HistorialAlumnoResponse;
import com.sindaco.academico.dto.ItemAcademico;
import com.sindaco.academico.dto.ItemFinanciero;
import com.sindaco.agenda.service.ClaseService;
import com.sindaco.comun.RangoFechas;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.matricula.service.AlumnoService;
import com.sindaco.matricula.service.MatriculaService;
import com.sindaco.matricula.service.PagoService;

/** Implementación del historial académico y financiero (CU10). */
@Service
@Transactional(readOnly = true)
public class HistorialServiceImpl implements HistorialService {

    private final AlumnoService alumnoService;
    private final MatriculaService matriculaService;
    private final PagoService pagoService;
    private final ClaseService claseService;
    private final AsistenciaService asistenciaService;

    public HistorialServiceImpl(AlumnoService alumnoService, MatriculaService matriculaService,
            PagoService pagoService, ClaseService claseService, AsistenciaService asistenciaService) {
        this.alumnoService = alumnoService;
        this.matriculaService = matriculaService;
        this.pagoService = pagoService;
        this.claseService = claseService;
        this.asistenciaService = asistenciaService;
    }

    @Override
    public HistorialAlumnoResponse porAlumno(Long alumnoId) {
        if (alumnoId == null) {
            throw new ReglaNegocioException("El id del alumno es obligatorio");
        }

        var alumno = alumnoService.buscarPorId(alumnoId);
        String alumnoNombre = nombreCompleto(texto(alumno.nombre()), texto(alumno.apellidos()));

        var matriculas = matriculaService.listarPorAlumno(alumnoId);
        Map<Long, String> estadosPorMatricula = new HashMap<>();
        if (matriculas != null) {
            for (var matricula : matriculas) {
                if (matricula.id() != null) {
                    estadosPorMatricula.put(matricula.id(), texto(matricula.estado()));
                }
            }
        }

        String paqueteActual = "";
        String estadoActual = "";
        if (matriculas != null && !matriculas.isEmpty()) {
            var masReciente = matriculas.stream()
                    .filter(m -> m.fecha() != null)
                    .max(Comparator.comparing(m -> m.fecha()))
                    .orElse(matriculas.get(0));
            paqueteActual = texto(masReciente.paquete());
            estadoActual = texto(masReciente.estado());
        }

        List<ItemAcademico> academico = construirAcademico(alumnoId, paqueteActual, estadoActual);
        List<ItemFinanciero> financiero = construirFinanciero(estadosPorMatricula);

        return new HistorialAlumnoResponse(alumnoId, alumnoNombre, academico, financiero);
    }

    private List<ItemAcademico> construirAcademico(Long alumnoId, String paquete, String estado) {
        List<ItemAcademico> items = new ArrayList<>();
        var asistencias = asistenciaService.listar();
        if (asistencias == null || asistencias.isEmpty()) {
            return items;
        }

        Set<Long> claseIds = asistencias.stream()
                .filter(a -> alumnoId.equals(a.alumnoId()))
                .map(AsistenciaResponse::claseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        for (Long claseId : claseIds) {
            String claseNombre = "";
            LocalDate fechaClase = null;
            try {
                var clase = claseService.buscarPorId(claseId);
                claseNombre = texto(clase.nombre());
                fechaClase = clase.fecha();
            } catch (RecursoNoEncontradoException ex) {
                // La clase pudo darse de baja: se conserva la información de asistencia disponible.
            }

            AsistenciaResponse propia = null;
            var asistenciasDeClase = asistenciaService.listarPorClase(claseId);
            if (asistenciasDeClase != null) {
                propia = asistenciasDeClase.stream()
                        .filter(a -> alumnoId.equals(a.alumnoId()))
                        .findFirst()
                        .orElse(null);
            }
            if (propia == null) {
                propia = asistencias.stream()
                        .filter(a -> alumnoId.equals(a.alumnoId()) && claseId.equals(a.claseId()))
                        .findFirst()
                        .orElse(null);
            }

            LocalDate fecha = fechaClase != null ? fechaClase : (propia == null ? null : propia.fecha());
            String asistencia = propia != null && propia.asistio() ? "ASISTIO" : "FALTA";
            Integer calificacion = propia == null ? null : propia.calificacion();
            items.add(new ItemAcademico(fecha, paquete, estado, claseNombre, asistencia, calificacion));
        }

        items.sort(Comparator.comparing(ItemAcademico::fecha, Comparator.nullsLast(Comparator.naturalOrder())));
        return items;
    }

    private List<ItemFinanciero> construirFinanciero(Map<Long, String> estadosPorMatricula) {
        List<ItemFinanciero> items = new ArrayList<>();
        var pagos = pagoService.listarPorFecha(RangoFechas.INICIO, RangoFechas.FIN);
        if (pagos == null || pagos.isEmpty()) {
            return items;
        }

        for (var pago : pagos) {
            if (pago.matriculaId() == null || !estadosPorMatricula.containsKey(pago.matriculaId())) {
                continue;
            }
            items.add(new ItemFinanciero(
                    pago.fecha(),
                    estadosPorMatricula.get(pago.matriculaId()),
                    pago.monto(),
                    texto(pago.comprobante())));
        }

        items.sort(Comparator.comparing(ItemFinanciero::fecha, Comparator.nullsLast(Comparator.naturalOrder())));
        return items;
    }

    private String nombreCompleto(String nombre, String apellidos) {
        if (apellidos == null || apellidos.isBlank()) {
            return nombre;
        }
        return nombre + " " + apellidos;
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
