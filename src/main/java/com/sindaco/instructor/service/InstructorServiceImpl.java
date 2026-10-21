package com.sindaco.instructor.service;

import com.sindaco.instructor.dto.InstructorRequest;
import com.sindaco.instructor.dto.InstructorResponse;
import com.sindaco.instructor.entity.Instructor;
import com.sindaco.instructor.repository.InstructorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRepository repository;

    public InstructorServiceImpl(InstructorRepository repository) {
        this.repository = repository;
    }

    @Override
    public InstructorResponse crear(InstructorRequest request) {
        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        Instructor instructor = new Instructor(
            request.getNombre(),
            request.getEspecialidad(),
            request.getTelefono(),
            request.getDisponible() != null ? request.getDisponible() : true
        );

        Instructor guardado = repository.save(instructor);

        return new InstructorResponse(
            guardado.getId(),
            guardado.getNombre(),
            guardado.getEspecialidad(),
            guardado.getTelefono(),
            guardado.getDisponible()
        );
    }

    @Override
    public List<InstructorResponse> listar() {
        return repository.findAll().stream()
            .map(i -> new InstructorResponse(
                i.getId(),
                i.getNombre(),
                i.getEspecialidad(),
                i.getTelefono(),
                i.getDisponible()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public InstructorResponse buscarPorId(Long id) {
        Instructor instructor = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Instructor no encontrado"));

        return new InstructorResponse(
            instructor.getId(),
            instructor.getNombre(),
            instructor.getEspecialidad(),
            instructor.getTelefono(),
            instructor.getDisponible()
        );
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Instructor no encontrado");
        }
        repository.deleteById(id);
    }
}
