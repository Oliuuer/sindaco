package com.sindaco.matricula.service;

import com.sindaco.matricula.dto.MatriculaRequest;
import com.sindaco.matricula.dto.MatriculaResponse;
import com.sindaco.matricula.entity.Matricula;
import com.sindaco.matricula.enum.EstadoMatricula;
import com.sindaco.matricula.repository.MatriculaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatriculaServiceImpl implements MatriculaService {

    private final MatriculaRepository repository;

    public MatriculaServiceImpl(MatriculaRepository repository) {
        this.repository = repository;
    }

    @Override
    public MatriculaResponse crear(MatriculaRequest request) {
        if (request.getAlumnoId() == null) {
            throw new IllegalArgumentException("El alumno es obligatorio");
        }

        if (request.getPaquete() == null || request.getPaquete().isBlank()) {
            throw new IllegalArgumentException("El paquete es obligatorio");
        }

        EstadoMatricula estado = request.getEstado() != null ? request.getEstado() : EstadoMatricula.POR_PAGAR;
        LocalDate fecha = request.getFecha() != null ? LocalDate.parse(request.getFecha()) : LocalDate.now();

        Matricula matricula = new Matricula(request.getAlumnoId(), request.getPaquete(), estado, fecha);
        Matricula guardada = repository.save(matricula);

        return new MatriculaResponse(
            guardada.getId(),
            guardada.getAlumnoId(),
            guardada.getPaquete(),
            guardada.getEstado(),
            guardada.getFecha()
        );
    }

    @Override
    public List<MatriculaResponse> listar() {
        return repository.findAll().stream()
            .map(m -> new MatriculaResponse(
                m.getId(),
                m.getAlumnoId(),
                m.getPaquete(),
                m.getEstado(),
                m.getFecha()
            ))
            .collect(Collectors.toList());
    }

    @Override
    public MatriculaResponse buscarPorId(Long id) {
        Matricula matricula = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));

        return new MatriculaResponse(
            matricula.getId(),
            matricula.getAlumnoId(),
            matricula.getPaquete(),
            matricula.getEstado(),
            matricula.getFecha()
        );
    }

    @Override
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Matrícula no encontrada");
        }
        repository.deleteById(id);
    }
}
