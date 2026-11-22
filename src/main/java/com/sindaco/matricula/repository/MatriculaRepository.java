package com.sindaco.matricula.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sindaco.matricula.entity.Matricula;

/** Acceso a datos de matrículas (CU01). */
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    /** Consulta JPQL: historial de matrículas de un alumno, de la más reciente a la más antigua. */
    @Query("SELECT m FROM Matricula m WHERE m.alumnoId = :alumnoId ORDER BY m.fecha DESC, m.id DESC")
    List<Matricula> findByAlumnoId(@Param("alumnoId") Long alumnoId);
}
