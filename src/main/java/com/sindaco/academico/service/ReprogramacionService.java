package com.sindaco.academico.service;

import java.util.List;

import com.sindaco.academico.dto.ReprogramacionRequest;
import com.sindaco.academico.dto.ReprogramacionResponse;

/** Casos de uso de reprogramaciones y control de ausencias (CU09). */
public interface ReprogramacionService {

    List<ReprogramacionResponse> listar();

    ReprogramacionResponse buscarPorId(Long id);

    ReprogramacionResponse crear(ReprogramacionRequest request);

    ReprogramacionResponse actualizar(Long id, ReprogramacionRequest request);

    void eliminar(Long id);
}
