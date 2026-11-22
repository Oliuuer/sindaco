package com.sindaco.matricula.service;

import java.util.List;

import com.sindaco.matricula.dto.AlumnoRequest;
import com.sindaco.matricula.dto.AlumnoResponse;

/** Casos de uso de alumnos (CU01). */
public interface AlumnoService {

    List<AlumnoResponse> listar();

    AlumnoResponse buscarPorId(Long id);

    AlumnoResponse crear(AlumnoRequest request);

    AlumnoResponse actualizar(Long id, AlumnoRequest request);

    void eliminar(Long id);
}
