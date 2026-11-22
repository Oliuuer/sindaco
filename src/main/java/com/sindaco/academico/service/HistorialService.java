package com.sindaco.academico.service;

import com.sindaco.academico.dto.HistorialAlumnoResponse;

/** Caso de uso de consulta del historial académico y financiero (CU10). */
public interface HistorialService {

    HistorialAlumnoResponse porAlumno(Long alumnoId);
}
