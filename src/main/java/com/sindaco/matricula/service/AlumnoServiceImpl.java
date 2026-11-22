package com.sindaco.matricula.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.comun.exception.ConflictoException;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.matricula.dto.AlumnoRequest;
import com.sindaco.matricula.dto.AlumnoResponse;
import com.sindaco.matricula.entity.Alumno;
import com.sindaco.matricula.repository.AlumnoRepository;

@Service
public class AlumnoServiceImpl implements AlumnoService {

    private final AlumnoRepository repository;

    public AlumnoServiceImpl(AlumnoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlumnoResponse> listar() {
        return repository.findAllOrdenados().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AlumnoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", id));
    }

    @Override
    @Transactional
    public AlumnoResponse crear(AlumnoRequest request) {
        String dni = request.dni().trim();
        validarDniDisponible(dni, null);

        Alumno alumno = new Alumno();
        aplicarRequest(alumno, request, dni);
        return aRespuesta(repository.save(alumno));
    }

    @Override
    @Transactional
    public AlumnoResponse actualizar(Long id, AlumnoRequest request) {
        Alumno alumno = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", id));

        String dni = request.dni().trim();
        validarDniDisponible(dni, id);
        aplicarRequest(alumno, request, dni);
        return aRespuesta(repository.save(alumno));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Alumno", id);
        }
        repository.deleteById(id);
    }

    private void validarDniDisponible(String dni, Long idActual) {
        repository.findByDni(dni)
                .filter(alumno -> idActual == null || !alumno.getId().equals(idActual))
                .ifPresent(alumno -> {
                    throw new ConflictoException("Ya existe un alumno con el DNI " + dni);
                });
    }

    private void aplicarRequest(Alumno alumno, AlumnoRequest request, String dni) {
        alumno.setNombre(request.nombre().trim());
        alumno.setApellidos(request.apellidos().trim());
        alumno.setDni(dni);
        alumno.setEmail(request.email().trim());
        alumno.setTelefono(request.telefono().trim());
        alumno.setDireccion(request.direccion().trim());
        alumno.setFechaRegistro(parsearFecha(request.fechaRegistro()));
    }

    private LocalDate parsearFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(fecha.trim());
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException("La fecha de registro debe tener el formato yyyy-MM-dd");
        }
    }

    private AlumnoResponse aRespuesta(Alumno alumno) {
        return new AlumnoResponse(
                alumno.getId(),
                alumno.getNombre(),
                alumno.getApellidos(),
                alumno.getDni(),
                alumno.getEmail(),
                alumno.getTelefono(),
                alumno.getDireccion(),
                alumno.getFechaRegistro());
    }
}
