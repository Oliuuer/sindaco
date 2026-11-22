package com.sindaco.academico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sindaco.academico.entity.AsistenciaEvaluacion;

/** Acceso a datos de asistencias y evaluaciones (CU05). */
public interface AsistenciaRepository extends JpaRepository<AsistenciaEvaluacion, Long> {

    List<AsistenciaEvaluacion> findByClaseId(Long claseId);

    List<AsistenciaEvaluacion> findByClaseIdOrderByFechaAsc(Long claseId);

    List<AsistenciaEvaluacion> findByAlumnoId(Long alumnoId);

    List<AsistenciaEvaluacion> findByAlumnoIdOrderByFechaAsc(Long alumnoId);

    boolean existsByClaseIdAndAlumnoId(Long claseId, Long alumnoId);

    boolean existsByClaseIdAndAlumnoIdAndIdNot(Long claseId, Long alumnoId, Long id);

    /** Asistencias con calificación registrada de una clase, ordenadas por fecha. */
    @Query("select a from AsistenciaEvaluacion a "
            + "where a.claseId = :claseId and a.calificacion is not null "
            + "order by a.fecha desc")
    List<AsistenciaEvaluacion> findCalificadasByClase(@Param("claseId") Long claseId);
}
