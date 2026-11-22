package com.sindaco.agenda.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.agenda.dto.InstructorRequest;
import com.sindaco.agenda.dto.InstructorResponse;
import com.sindaco.agenda.entity.Instructor;
import com.sindaco.agenda.repository.InstructorRepository;
import com.sindaco.comun.exception.RecursoNoEncontradoException;

/** Implementación de los casos de uso de instructores (CU04). */
@Service
@Transactional
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository repository;

    public InstructorServiceImpl(InstructorRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstructorResponse> listar() {
        return repository.findAll().stream().map(this::mapear).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorResponse buscarPorId(Long id) {
        return mapear(buscarEntidad(id));
    }

    @Override
    public InstructorResponse crear(InstructorRequest request) {
        Instructor instructor = new Instructor(
                request.nombre(),
                request.especialidad(),
                request.telefono(),
                request.disponible() != null ? request.disponible() : Boolean.TRUE);
        return mapear(repository.save(instructor));
    }

    @Override
    public InstructorResponse actualizar(Long id, InstructorRequest request) {
        Instructor instructor = buscarEntidad(id);
        instructor.setNombre(request.nombre());
        instructor.setEspecialidad(request.especialidad());
        instructor.setTelefono(request.telefono());
        instructor.setDisponible(request.disponible() != null ? request.disponible() : Boolean.TRUE);
        return mapear(repository.save(instructor));
    }

    @Override
    public void eliminar(Long id) {
        repository.delete(buscarEntidad(id));
    }

    private Instructor buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Instructor", id));
    }

    private InstructorResponse mapear(Instructor instructor) {
        return new InstructorResponse(
                instructor.getId(),
                instructor.getNombre(),
                instructor.getEspecialidad(),
                instructor.getTelefono(),
                instructor.getDisponible());
    }
}
