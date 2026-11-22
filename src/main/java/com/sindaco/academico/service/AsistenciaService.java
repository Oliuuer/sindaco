package com.sindaco.academico.service;

import java.util.List;

import com.sindaco.academico.dto.AsistenciaRequest;
import com.sindaco.academico.dto.AsistenciaResponse;

/** Casos de uso de asistencia y evaluación (CU05). */
public interface AsistenciaService {

    List<AsistenciaResponse> listar();

    List<AsistenciaResponse> listarPorClase(Long claseId);

    AsistenciaResponse buscarPorId(Long id);

    AsistenciaResponse crear(AsistenciaRequest request);

    AsistenciaResponse actualizar(Long id, AsistenciaRequest request);

    void eliminar(Long id);
}
