package com.sindaco.matricula.service;

import java.util.List;

import com.sindaco.matricula.dto.MatriculaRequest;
import com.sindaco.matricula.dto.MatriculaResponse;

/** Casos de uso de matrículas (CU01). */
public interface MatriculaService {

    List<MatriculaResponse> listar();

    MatriculaResponse buscarPorId(Long id);

    MatriculaResponse crear(MatriculaRequest request);

    MatriculaResponse actualizar(Long id, MatriculaRequest request);

    void eliminar(Long id);

    List<MatriculaResponse> listarPorAlumno(Long alumnoId);
}
