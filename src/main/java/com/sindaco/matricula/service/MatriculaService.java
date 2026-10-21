package com.sindaco.matricula.service;

import com.sindaco.matricula.dto.MatriculaRequest;
import com.sindaco.matricula.dto.MatriculaResponse;

import java.util.List;

public interface MatriculaService {
    MatriculaResponse crear(MatriculaRequest request);
    List<MatriculaResponse> listar();
    MatriculaResponse buscarPorId(Long id);
    void eliminar(Long id);
}
