package com.sindaco.instructor.service;

import com.sindaco.instructor.dto.InstructorRequest;
import com.sindaco.instructor.dto.InstructorResponse;

import java.util.List;

public interface InstructorService {
    InstructorResponse crear(InstructorRequest request);
    List<InstructorResponse> listar();
    InstructorResponse buscarPorId(Long id);
    void eliminar(Long id);
}
