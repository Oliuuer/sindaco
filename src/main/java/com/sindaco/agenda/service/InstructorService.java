package com.sindaco.agenda.service;

import java.util.List;

import com.sindaco.agenda.dto.InstructorRequest;
import com.sindaco.agenda.dto.InstructorResponse;

/** Casos de uso de instructores (CU04). */
public interface InstructorService {

    List<InstructorResponse> listar();

    InstructorResponse buscarPorId(Long id);

    InstructorResponse crear(InstructorRequest request);

    InstructorResponse actualizar(Long id, InstructorRequest request);

    void eliminar(Long id);
}
