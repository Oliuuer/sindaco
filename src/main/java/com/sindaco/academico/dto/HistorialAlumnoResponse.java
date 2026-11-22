package com.sindaco.academico.dto;

import java.util.List;

/** Historial académico y financiero consolidado de un alumno (CU10). */
public record HistorialAlumnoResponse(
        Long alumnoId,
        String alumnoNombre,
        List<ItemAcademico> academico,
        List<ItemFinanciero> financiero) {
}
