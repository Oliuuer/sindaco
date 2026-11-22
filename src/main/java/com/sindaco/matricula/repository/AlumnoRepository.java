package com.sindaco.matricula.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sindaco.matricula.entity.Alumno;

/** Acceso a datos de alumnos (CU01). */
public interface AlumnoRepository extends JpaRepository<Alumno, Long> {

    /** Consulta JPQL: permite detectar un DNI duplicado antes de guardar. */
    @Query("SELECT a FROM Alumno a WHERE a.dni = :dni")
    Optional<Alumno> findByDni(@Param("dni") String dni);

    @Query("SELECT a FROM Alumno a ORDER BY a.apellidos ASC, a.nombre ASC")
    List<Alumno> findAllOrdenados();
}
