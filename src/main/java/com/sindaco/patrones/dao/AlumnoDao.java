package com.sindaco.patrones.dao;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object del alumno. Define las operaciones de acceso a la tabla
 * 'alumnos' de PostgreSQL, devolviendo siempre DTOs.
 */
public interface AlumnoDao {

    List<AlumnoDto> listar();

    Optional<AlumnoDto> buscarPorId(Long id);

    int guardar(AlumnoDto alumno);
}
