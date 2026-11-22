package com.sindaco.matricula.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.comun.notificacion.service.NotificacionService;
import com.sindaco.matricula.dto.MatriculaRequest;
import com.sindaco.matricula.dto.MatriculaResponse;
import com.sindaco.matricula.entity.Alumno;
import com.sindaco.matricula.entity.Matricula;
import com.sindaco.matricula.entity.Paquete;
import com.sindaco.matricula.enums.EstadoMatricula;
import com.sindaco.matricula.promocion.service.PromocionService;
import com.sindaco.matricula.repository.AlumnoRepository;
import com.sindaco.matricula.repository.MatriculaRepository;
import com.sindaco.matricula.repository.PaqueteRepository;

@Service
public class MatriculaServiceImpl implements MatriculaService {

    private final MatriculaRepository repository;
    private final AlumnoRepository alumnoRepository;
    private final PaqueteRepository paqueteRepository;
    private final PromocionService promocionService;
    private final NotificacionService notificacionService;

    public MatriculaServiceImpl(MatriculaRepository repository, AlumnoRepository alumnoRepository,
            PaqueteRepository paqueteRepository, PromocionService promocionService,
            NotificacionService notificacionService) {
        this.repository = repository;
        this.alumnoRepository = alumnoRepository;
        this.paqueteRepository = paqueteRepository;
        this.promocionService = promocionService;
        this.notificacionService = notificacionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponse> listar() {
        return repository.findAll().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponse> listarPorAlumno(Long alumnoId) {
        return repository.findByAlumnoId(alumnoId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MatriculaResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula", id));
    }

    @Override
    @Transactional
    public MatriculaResponse crear(MatriculaRequest request) {
        validarAlumno(request.alumnoId());
        Paquete paquete = paqueteVigente(request.paqueteId());

        EstadoMatricula estado = request.estado() != null ? request.estado() : EstadoMatricula.POR_PAGAR;
        Matricula matricula = new Matricula(
                request.alumnoId(),
                request.paqueteId(),
                estado,
                parsearFecha(request.fecha()));

        aplicarPromocion(matricula, paquete, request.codigoPromocion());
        MatriculaResponse respuesta = aRespuesta(repository.save(matricula));

        notificacionService.registrar("MATRICULA", "Matrícula registrada",
                "Se registró la matrícula del alumno " + respuesta.alumnoNombre()
                        + " en el paquete " + respuesta.paquete(),
                "RECEPCIONISTA", request.alumnoId());
        return respuesta;
    }

    @Override
    @Transactional
    public MatriculaResponse actualizar(Long id, MatriculaRequest request) {
        Matricula matricula = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula", id));

        validarAlumno(request.alumnoId());
        Paquete paquete = paqueteVigente(request.paqueteId());

        matricula.setAlumnoId(request.alumnoId());
        matricula.setPaqueteId(request.paqueteId());
        matricula.setEstado(request.estado() != null ? request.estado() : EstadoMatricula.POR_PAGAR);
        matricula.setFecha(parsearFecha(request.fecha()));
        aplicarPromocion(matricula, paquete, request.codigoPromocion());

        return aRespuesta(repository.save(matricula));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (!repository.existsById(id)) {
            throw new RecursoNoEncontradoException("Matrícula", id);
        }
        repository.deleteById(id);
    }

    /** Aplica la promoción vigente, si se indicó, y guarda el precio final y el descuento. */
    private void aplicarPromocion(Matricula matricula, Paquete paquete, String codigoPromocion) {
        BigDecimal precioBase = paquete.getPrecio();
        String codigo = codigoPromocion == null || codigoPromocion.isBlank()
                ? null
                : codigoPromocion.trim().toUpperCase(java.util.Locale.ROOT);

        BigDecimal precioFinal = promocionService.aplicarDescuento(precioBase, codigo);
        BigDecimal descuento = precioBase.subtract(precioFinal).setScale(2, RoundingMode.HALF_UP);

        matricula.setPrecioFinal(precioFinal);
        matricula.setDescuento(descuento);
        matricula.setCodigoPromocion(codigo);
    }

    private void validarAlumno(Long alumnoId) {
        alumnoRepository.findById(alumnoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Alumno", alumnoId));
    }

    private Paquete paqueteVigente(Long paqueteId) {
        Paquete paquete = paqueteRepository.findById(paqueteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete", paqueteId));
        if (!paquete.isVigente()) {
            throw new ReglaNegocioException("No se puede registrar una matrícula con un paquete que no está vigente");
        }
        return paquete;
    }

    private LocalDate parsearFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(fecha.trim());
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException("La fecha debe tener el formato yyyy-MM-dd");
        }
    }

    private MatriculaResponse aRespuesta(Matricula matricula) {
        Alumno alumno = alumnoRepository.findById(matricula.getAlumnoId()).orElse(null);
        Paquete paquete = paqueteRepository.findById(matricula.getPaqueteId()).orElse(null);
        String alumnoNombre = alumno == null ? "" : alumno.getNombre() + " " + alumno.getApellidos();
        String paqueteNombre = paquete == null ? "" : paquete.getNombre();

        BigDecimal precioFinal = matricula.getPrecioFinal();
        if (precioFinal == null) {
            precioFinal = paquete == null ? BigDecimal.ZERO : paquete.getPrecio();
        }
        BigDecimal descuento = matricula.getDescuento() == null ? BigDecimal.ZERO : matricula.getDescuento();

        return new MatriculaResponse(
                matricula.getId(),
                matricula.getAlumnoId(),
                alumnoNombre,
                matricula.getPaqueteId(),
                paqueteNombre,
                matricula.getEstado(),
                matricula.getFecha(),
                precioFinal,
                descuento,
                matricula.getCodigoPromocion());
    }
}
