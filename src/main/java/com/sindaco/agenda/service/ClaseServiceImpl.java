package com.sindaco.agenda.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;
import com.sindaco.agenda.dto.InstructorResponse;
import com.sindaco.agenda.dto.VehiculoResponse;
import com.sindaco.agenda.entity.Clase;
import com.sindaco.agenda.enums.EstadoClase;
import com.sindaco.agenda.enums.EstadoVehiculo;
import com.sindaco.agenda.repository.ClaseRepository;
import com.sindaco.comun.exception.ConflictoException;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.matricula.dto.AlumnoResponse;
import com.sindaco.matricula.service.AlumnoService;

/** Implementación de los casos de uso de la agenda de clases (CU03). */
@Service
@Transactional
public class ClaseServiceImpl implements ClaseService {

    private final ClaseRepository repository;
    private final InstructorService instructorService;
    private final VehiculoService vehiculoService;
    private final AlumnoService alumnoService;

    public ClaseServiceImpl(ClaseRepository repository,
                            InstructorService instructorService,
                            VehiculoService vehiculoService,
                            AlumnoService alumnoService) {
        this.repository = repository;
        this.instructorService = instructorService;
        this.vehiculoService = vehiculoService;
        this.alumnoService = alumnoService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaseResponse> listar() {
        return repository.findAll().stream().map(this::mapear).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaseResponse> listarPorInstructor(Long instructorId) {
        instructorService.buscarPorId(instructorId);
        return repository.findByInstructorId(instructorId).stream().map(this::mapear).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClaseResponse> listarPorAlumno(Long alumnoId) {
        alumnoService.buscarPorId(alumnoId);
        return repository.findByAlumnoIdOrderByFechaAscHoraInicioAsc(alumnoId).stream()
                .map(this::mapear)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClaseResponse buscarPorId(Long id) {
        return mapear(buscarEntidad(id));
    }

    @Override
    public ClaseResponse crear(ClaseRequest request) {
        DatosClase datos = validar(request, null);
        Clase clase = new Clase(
                request.nombre(),
                request.alumnoId(),
                request.instructorId(),
                request.vehiculoId(),
                datos.fecha(),
                datos.horaInicio(),
                datos.horaFin(),
                datos.estado());
        return mapear(repository.save(clase));
    }

    @Override
    public ClaseResponse actualizar(Long id, ClaseRequest request) {
        Clase clase = buscarEntidad(id);
        DatosClase datos = validar(request, id);
        clase.setNombre(request.nombre());
        clase.setAlumnoId(request.alumnoId());
        clase.setInstructorId(request.instructorId());
        clase.setVehiculoId(request.vehiculoId());
        clase.setFecha(datos.fecha());
        clase.setHoraInicio(datos.horaInicio());
        clase.setHoraFin(datos.horaFin());
        clase.setEstado(datos.estado());
        return mapear(repository.save(clase));
    }

    @Override
    public void eliminar(Long id) {
        repository.delete(buscarEntidad(id));
    }

    @Override
    @Transactional(readOnly = true)
    public void validarDisponibilidad(ClaseRequest request, Long idClase) {
        validar(request, idClase);
    }

    /**
     * Aplica todas las reglas de negocio compartidas por crear y actualizar:
     * existencia de alumno, instructor y vehículo, disponibilidad, estado
     * operativo, orden de horas y ausencia de solapamientos de horario.
     */
    private DatosClase validar(ClaseRequest request, Long idActual) {
        LocalDate fecha = parsearFecha(request.fecha());
        LocalTime horaInicio = parsearHora(request.horaInicio(), "inicio");
        LocalTime horaFin = parsearHora(request.horaFin(), "fin");

        if (!horaFin.isAfter(horaInicio)) {
            throw new ReglaNegocioException("La hora de fin debe ser posterior a la hora de inicio");
        }

        // El servicio de matrícula resuelve la existencia del alumno (nunca su repositorio).
        alumnoService.buscarPorId(request.alumnoId());
        InstructorResponse instructor = instructorService.buscarPorId(request.instructorId());
        VehiculoResponse vehiculo = vehiculoService.buscarPorId(request.vehiculoId());

        if (!Boolean.TRUE.equals(instructor.disponible())) {
            throw new ReglaNegocioException("El instructor seleccionado no está disponible");
        }
        if (vehiculo.estado() != EstadoVehiculo.OPERATIVO) {
            throw new ReglaNegocioException("El vehículo seleccionado no está operativo");
        }
        if (repository.existeSolapamiento(idActual, request.instructorId(), request.vehiculoId(),
                fecha, horaInicio, horaFin)) {
            throw new ConflictoException("El instructor o el vehículo ya tienen una clase asignada en ese horario");
        }

        return new DatosClase(fecha, horaInicio, horaFin, parsearEstado(request.estado()));
    }

    private Clase buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Clase", id));
    }

    private LocalDate parsearFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            throw new ReglaNegocioException("La fecha de la clase es obligatoria");
        }
        try {
            return LocalDate.parse(fecha.trim());
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException("La fecha de la clase debe tener el formato yyyy-MM-dd");
        }
    }

    private LocalTime parsearHora(String hora, String campo) {
        if (hora == null || hora.isBlank()) {
            throw new ReglaNegocioException("La hora de " + campo + " de la clase es obligatoria");
        }
        try {
            return LocalTime.parse(hora.trim());
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException("La hora de " + campo + " de la clase debe tener el formato HH:mm");
        }
    }

    /** El estado es opcional; por omisión la clase queda PROGRAMADA. */
    private EstadoClase parsearEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return EstadoClase.PROGRAMADA;
        }
        try {
            return EstadoClase.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException("Estado de clase inválido: " + estado);
        }
    }

    private ClaseResponse mapear(Clase clase) {
        AlumnoResponse alumno = alumnoService.buscarPorId(clase.getAlumnoId());
        String instructorNombre = instructorService.buscarPorId(clase.getInstructorId()).nombre();
        String vehiculoPlaca = vehiculoService.buscarPorId(clase.getVehiculoId()).placa();

        return new ClaseResponse(
                clase.getId(),
                clase.getNombre(),
                clase.getFecha(),
                clase.getHoraInicio(),
                clase.getHoraFin(),
                clase.getAlumnoId(),
                clase.getInstructorId(),
                clase.getVehiculoId(),
                nombreCompleto(alumno),
                instructorNombre,
                vehiculoPlaca,
                clase.getEstado());
    }

    private String nombreCompleto(AlumnoResponse alumno) {
        String nombre = alumno.nombre() == null ? "" : alumno.nombre().trim();
        String apellidos = alumno.apellidos() == null ? "" : alumno.apellidos().trim();
        return (nombre + " " + apellidos).trim();
    }

    /** Valores ya convertidos y validados de una petición de clase. */
    private record DatosClase(LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, EstadoClase estado) {
    }
}
