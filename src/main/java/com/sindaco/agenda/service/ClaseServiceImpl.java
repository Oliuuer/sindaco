package com.sindaco.agenda.service;

import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;
import com.sindaco.agenda.entity.Clase;
import com.sindaco.agenda.repository.ClaseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClaseServiceImpl implements ClaseService {

    private final ClaseRepository repository;

    public ClaseServiceImpl(ClaseRepository repository) {
        this.repository = repository;
    }

    @Override
    public ClaseResponse crear(ClaseRequest request) {
        if (request.getInstructorId() == null || request.getAlumnoId() == null) {
            throw new IllegalArgumentException("Instructor y alumno son obligatorios");
        }

        Clase clase = new Clase(
            request.getInstructorId(),
            request.getAlumnoId(),
            LocalDate.parse(request.getFecha()),
            LocalTime.parse(request.getHoraInicio()),
            LocalTime.parse(request.getHoraFin()),
            request.getEstado() != null ? request.getEstado() : "PROGRAMADA"
        );

        Clase guardada = repository.save(clase);

        return new ClaseResponse(
            guardada.getId(),
            guardada.getInstructorId(),
            guardada.getAlumnoId(),
            guardada.getFecha(),
            guardada.getHoraInicio(),
            guardada.getHoraFin(),
            guardada.getEstado()
        );
    }

    @Override
    public List<ClaseResponse> listar() {
        return repository.findAll().stream()
            .map(c -> new ClaseResponse(
                c.getId(),
                c.getInstructorId(),
                c.getAlumnoId(),
                c.getFecha(),
                c.getHoraInicio(),
                c.getHoraFin(),
                c.getEstado()
            ))
            .collect(Collectors.toList());
    }
}
