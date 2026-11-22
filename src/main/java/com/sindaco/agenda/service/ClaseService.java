package com.sindaco.agenda.service;

import java.util.List;

import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;

/** Casos de uso de la agenda de clases (CU03). */
public interface ClaseService {

    List<ClaseResponse> listar();

    ClaseResponse buscarPorId(Long id);

    ClaseResponse crear(ClaseRequest request);

    ClaseResponse actualizar(Long id, ClaseRequest request);

    void eliminar(Long id);

    /** Clases asignadas a un instructor; útil para otros módulos (académico, reportes). */
    List<ClaseResponse> listarPorInstructor(Long instructorId);
}
