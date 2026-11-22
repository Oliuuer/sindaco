package com.sindaco.academico.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.academico.dto.AsistenciaRequest;
import com.sindaco.academico.dto.AsistenciaResponse;
import com.sindaco.academico.entity.AsistenciaEvaluacion;
import com.sindaco.academico.repository.AsistenciaRepository;
import com.sindaco.agenda.service.ClaseService;
import com.sindaco.comun.exception.ConflictoException;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.matricula.service.AlumnoService;

/** Implementación de asistencia y evaluación (CU05). */
@Service
@Transactional(readOnly = true)
public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaRepository repository;
    private final ClaseService claseService;
    private final AlumnoService alumnoService;

    public AsistenciaServiceImpl(AsistenciaRepository repository, ClaseService claseService,
            AlumnoService alumnoService) {
        this.repository = repository;
        this.claseService = claseService;
        this.alumnoService = alumnoService;
    }

    @Override
    public List<AsistenciaResponse> listar() {
        return repository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Override
    public List<AsistenciaResponse> listarPorClase(Long claseId) {
        if (claseId == null) {
            throw new ReglaNegocioException("El id de la clase es obligatorio");
        }
        return repository.findByClaseIdOrderByFechaAsc(claseId).stream().map(this::aRespuesta).toList();
    }

    @Override
    public AsistenciaResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asistencia", id));
    }

    @Override
    @Transactional
    public AsistenciaResponse crear(AsistenciaRequest request) {
        validar(request);
        var clase = claseService.buscarPorId(request.claseId());
        Long alumnoId = clase.alumnoId();
        if (alumnoId == null) {
            throw new ReglaNegocioException("La clase seleccionada no tiene un alumno asignado");
        }
        alumnoService.buscarPorId(alumnoId);
        if (repository.existsByClaseIdAndAlumnoId(request.claseId(), alumnoId)) {
            throw new ConflictoException("Ya existe una asistencia registrada para esta clase y este alumno");
        }

        AsistenciaEvaluacion asistencia = new AsistenciaEvaluacion();
        asistencia.setClaseId(request.claseId());
        asistencia.setAlumnoId(alumnoId);
        asistencia.setAsistio(Boolean.TRUE.equals(request.asistio()));
        asistencia.setCalificacion(request.calificacion());
        asistencia.setObservaciones(normalizar(request.observaciones()));
        asistencia.setFecha(LocalDate.now());
        return aRespuesta(repository.save(asistencia));
    }

    @Override
    @Transactional
    public AsistenciaResponse actualizar(Long id, AsistenciaRequest request) {
        AsistenciaEvaluacion asistencia = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asistencia", id));
        validar(request);
        var clase = claseService.buscarPorId(request.claseId());
        Long alumnoId = clase.alumnoId();
        if (alumnoId == null) {
            throw new ReglaNegocioException("La clase seleccionada no tiene un alumno asignado");
        }
        alumnoService.buscarPorId(alumnoId);
        if (repository.existsByClaseIdAndAlumnoIdAndIdNot(request.claseId(), alumnoId, id)) {
            throw new ConflictoException("Ya existe una asistencia registrada para esta clase y este alumno");
        }

        asistencia.setClaseId(request.claseId());
        asistencia.setAlumnoId(alumnoId);
        asistencia.setAsistio(Boolean.TRUE.equals(request.asistio()));
        asistencia.setCalificacion(request.calificacion());
        asistencia.setObservaciones(normalizar(request.observaciones()));
        return aRespuesta(repository.save(asistencia));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        AsistenciaEvaluacion asistencia = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asistencia", id));
        repository.delete(asistencia);
    }

    private void validar(AsistenciaRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("Los datos de la asistencia son obligatorios");
        }
        if (request.claseId() == null) {
            throw new ReglaNegocioException("El id de la clase es obligatorio");
        }
        if (request.asistio() == null) {
            throw new ReglaNegocioException("El campo asistio es obligatorio");
        }
        if (request.calificacion() == null) {
            throw new ReglaNegocioException("La calificación es obligatoria");
        }
        if (request.calificacion() < 0 || request.calificacion() > 20) {
            throw new ReglaNegocioException("La calificación debe estar entre 0 y 20");
        }
    }

    private AsistenciaResponse aRespuesta(AsistenciaEvaluacion a) {
        return new AsistenciaResponse(
                a.getId(),
                a.getClaseId(),
                nombreClase(a.getClaseId()),
                a.getAlumnoId(),
                nombreAlumno(a.getAlumnoId()),
                a.isAsistio(),
                a.getCalificacion(),
                a.getObservaciones(),
                a.getFecha());
    }

    private String nombreClase(Long claseId) {
        if (claseId == null) {
            return "";
        }
        try {
            return texto(claseService.buscarPorId(claseId).nombre());
        } catch (RecursoNoEncontradoException ex) {
            return "";
        }
    }

    private String nombreAlumno(Long alumnoId) {
        if (alumnoId == null) {
            return "";
        }
        try {
            var alumno = alumnoService.buscarPorId(alumnoId);
            return nombreCompleto(texto(alumno.nombre()), texto(alumno.apellidos()));
        } catch (RecursoNoEncontradoException ex) {
            return "";
        }
    }

    private String nombreCompleto(String nombre, String apellidos) {
        if (apellidos == null || apellidos.isBlank()) {
            return nombre;
        }
        return nombre + " " + apellidos;
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
