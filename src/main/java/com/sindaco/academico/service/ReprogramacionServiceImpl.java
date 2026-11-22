package com.sindaco.academico.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.academico.dto.ReprogramacionRequest;
import com.sindaco.academico.dto.ReprogramacionResponse;
import com.sindaco.academico.entity.Reprogramacion;
import com.sindaco.academico.enums.EstadoReprogramacion;
import com.sindaco.academico.repository.ReprogramacionRepository;
import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;
import com.sindaco.agenda.enums.EstadoClase;
import com.sindaco.agenda.service.ClaseService;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.comun.notificacion.service.NotificacionService;
import com.sindaco.matricula.service.AlumnoService;

/** Implementación de reprogramaciones y control de ausencias (CU09). */
@Service
@Transactional(readOnly = true)
public class ReprogramacionServiceImpl implements ReprogramacionService {

    private final ReprogramacionRepository repository;
    private final ClaseService claseService;
    private final AlumnoService alumnoService;
    private final NotificacionService notificacionService;

    public ReprogramacionServiceImpl(ReprogramacionRepository repository, ClaseService claseService,
            AlumnoService alumnoService, NotificacionService notificacionService) {
        this.repository = repository;
        this.claseService = claseService;
        this.alumnoService = alumnoService;
        this.notificacionService = notificacionService;
    }

    @Override
    public List<ReprogramacionResponse> listar() {
        return repository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Override
    public ReprogramacionResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reprogramación", id));
    }

    @Override
    @Transactional
    public ReprogramacionResponse crear(ReprogramacionRequest request) {
        validar(request);
        alumnoService.buscarPorId(request.alumnoId());
        ClaseResponse clase = claseService.buscarPorId(request.claseId());

        ClaseRequest nuevaClase = construirClaseReprogramada(request, clase);
        claseService.validarDisponibilidad(nuevaClase, clase.id());
        claseService.actualizar(clase.id(), nuevaClase);

        Reprogramacion reprogramacion = new Reprogramacion();
        aplicar(reprogramacion, request, clase.fecha());
        ReprogramacionResponse respuesta = aRespuesta(repository.save(reprogramacion));

        notificacionService.registrar("REPROGRAMACION", "Clase reprogramada",
                "Se reprogramó la clase \"" + texto(clase.nombre()) + "\" para el "
                        + respuesta.nuevaFecha() + " a las " + texto(respuesta.nuevaHoraInicio())
                        + ". Motivo: " + respuesta.motivo(),
                "INSTRUCTOR", request.alumnoId());
        return respuesta;
    }

    @Override
    @Transactional
    public ReprogramacionResponse actualizar(Long id, ReprogramacionRequest request) {
        Reprogramacion reprogramacion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reprogramación", id));
        validar(request);
        alumnoService.buscarPorId(request.alumnoId());
        var clase = claseService.buscarPorId(request.claseId());

        aplicar(reprogramacion, request, clase.fecha());
        return aRespuesta(repository.save(reprogramacion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Reprogramacion reprogramacion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reprogramación", id));
        repository.delete(reprogramacion);
    }

    /** Si el frente no envía un dato nuevo, se conserva el de la clase original. */
    private ClaseRequest construirClaseReprogramada(ReprogramacionRequest request, ClaseResponse clase) {
        String fecha = request.nuevaFecha() != null && !request.nuevaFecha().isBlank()
                ? request.nuevaFecha()
                : texto(clase.fecha());
        String inicio = request.nuevaHoraInicio() != null && !request.nuevaHoraInicio().isBlank()
                ? request.nuevaHoraInicio()
                : texto(clase.horaInicio());
        String fin = request.nuevaHoraFin() != null && !request.nuevaHoraFin().isBlank()
                ? request.nuevaHoraFin()
                : texto(clase.horaFin());

        return new ClaseRequest(
                clase.nombre(),
                fecha,
                inicio,
                fin,
                clase.alumnoId(),
                clase.instructorId(),
                clase.vehiculoId(),
                EstadoClase.CONFIRMADA.name());
    }

    private void validar(ReprogramacionRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("Los datos de la reprogramación son obligatorios");
        }
        if (request.alumnoId() == null) {
            throw new ReglaNegocioException("El id del alumno es obligatorio");
        }
        if (request.claseId() == null) {
            throw new ReglaNegocioException("El id de la clase es obligatorio");
        }
        if (request.motivo() == null || request.motivo().isBlank()) {
            throw new ReglaNegocioException("El motivo de la reprogramación es obligatorio");
        }
    }

    private void aplicar(Reprogramacion destino, ReprogramacionRequest request, LocalDate fechaClase) {
        destino.setAlumnoId(request.alumnoId());
        destino.setClaseId(request.claseId());
        destino.setMotivo(request.motivo().trim());
        destino.setFecha(parsearFecha(request.fecha(), LocalDate.now(), "fecha"));
        destino.setNuevaFecha(parsearFecha(request.nuevaFecha(), fechaClase, "nueva fecha"));
        destino.setNuevaHoraInicio(parsearHora(request.nuevaHoraInicio(), "nueva hora de inicio"));
        destino.setNuevaHoraFin(parsearHora(request.nuevaHoraFin(), "nueva hora de fin"));
        destino.setEstado(parsearEstado(request.estado()));
    }

    private LocalDate parsearFecha(String valor, LocalDate porDefecto, String campo) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException("El formato de " + campo + " debe ser yyyy-MM-dd");
        }
    }

    private LocalTime parsearHora(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException("El formato de " + campo + " debe ser HH:mm");
        }
    }

    private EstadoReprogramacion parsearEstado(String valor) {
        if (valor == null || valor.isBlank()) {
            return EstadoReprogramacion.PENDIENTE;
        }
        try {
            return EstadoReprogramacion.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException("El estado de la reprogramación no es válido");
        }
    }

    private ReprogramacionResponse aRespuesta(Reprogramacion r) {
        return new ReprogramacionResponse(
                r.getId(),
                r.getAlumnoId(),
                nombreAlumno(r.getAlumnoId()),
                r.getClaseId(),
                nombreClase(r.getClaseId()),
                r.getMotivo(),
                r.getFecha(),
                r.getNuevaFecha(),
                r.getNuevaHoraInicio(),
                r.getNuevaHoraFin(),
                r.getEstado());
    }

    private String nombreAlumno(Long alumnoId) {
        if (alumnoId == null) {
            return "";
        }
        try {
            var alumno = alumnoService.buscarPorId(alumnoId);
            String nombre = texto(alumno.nombre());
            String apellidos = texto(alumno.apellidos());
            return apellidos.isBlank() ? nombre : nombre + " " + apellidos;
        } catch (RecursoNoEncontradoException ex) {
            return "";
        }
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

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
