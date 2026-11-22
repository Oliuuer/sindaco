package com.sindaco.matricula.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.comun.RangoFechas;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.comun.notificacion.service.NotificacionService;
import com.sindaco.matricula.dto.ComprobanteResponse;
import com.sindaco.matricula.dto.PagoRequest;
import com.sindaco.matricula.dto.PagoResponse;
import com.sindaco.matricula.entity.Matricula;
import com.sindaco.matricula.entity.Pago;
import com.sindaco.matricula.entity.Paquete;
import com.sindaco.matricula.enums.EstadoMatricula;
import com.sindaco.matricula.enums.MetodoPago;
import com.sindaco.matricula.enums.TipoPago;
import com.sindaco.matricula.repository.AlumnoRepository;
import com.sindaco.matricula.repository.MatriculaRepository;
import com.sindaco.matricula.repository.PagoRepository;
import com.sindaco.matricula.repository.PaqueteRepository;

@Service("matriculaPagoServiceImpl")
public class PagoServiceImpl implements PagoService {

    private final PagoRepository repository;
    private final MatriculaRepository matriculaRepository;
    private final AlumnoRepository alumnoRepository;
    private final PaqueteRepository paqueteRepository;
    private final NotificacionService notificacionService;

    public PagoServiceImpl(PagoRepository repository, MatriculaRepository matriculaRepository,
            AlumnoRepository alumnoRepository, PaqueteRepository paqueteRepository,
            NotificacionService notificacionService) {
        this.repository = repository;
        this.matriculaRepository = matriculaRepository;
        this.alumnoRepository = alumnoRepository;
        this.paqueteRepository = paqueteRepository;
        this.notificacionService = notificacionService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return repository.findAll().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorMatricula(Long matriculaId) {
        return repository.findByMatriculaId(matriculaId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorAlumno(Long alumnoId) {
        return repository.findByAlumnoId(alumnoId).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorFecha(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new ReglaNegocioException("Las fechas desde y hasta son obligatorias");
        }
        LocalDate inicio = RangoFechas.inicio(desde);
        LocalDate fin = RangoFechas.fin(hasta);
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha desde no puede ser posterior a la fecha hasta");
        }
        return repository.findByFechaBetween(inicio, fin).stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    @Override
    @Transactional
    public PagoResponse crear(PagoRequest request) {
        Matricula matricula = matriculaRepository.findById(request.matriculaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula", request.matriculaId()));
        validarMonto(request.monto());

        BigDecimal saldo = saldoPendiente(matricula);
        if (request.monto().compareTo(saldo) > 0) {
            throw new ReglaNegocioException("El monto " + request.monto().toPlainString()
                    + " supera el saldo pendiente de " + saldo.toPlainString());
        }

        TipoPago tipo = request.monto().compareTo(saldo) == 0 ? TipoPago.TOTAL : TipoPago.FRACCIONADO;
        String comprobante = request.comprobante() == null || request.comprobante().isBlank()
                ? null
                : request.comprobante().trim();

        Pago pago = new Pago(
                request.matriculaId(),
                request.monto(),
                normalizarMetodo(request.metodo()),
                comprobante,
                parsearFecha(request.fecha()),
                tipo);
        Pago guardado = repository.save(pago);

        if (guardado.getComprobante() == null) {
            guardado.setComprobante(String.format("B-%06d", guardado.getId()));
            guardado = repository.save(guardado);
        }

        actualizarEstadoMatricula(matricula);
        notificacionService.registrar("PAGO", "Pago registrado",
                "Se registró un pago de " + guardado.getMonto().toPlainString()
                        + " (" + tipo.name() + ") para la matrícula " + guardado.getMatriculaId(),
                "ADMINISTRADOR", matricula.getAlumnoId());

        return aRespuesta(guardado);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Pago pago = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
        repository.delete(pago);

        matriculaRepository.findById(pago.getMatriculaId()).ifPresent(this::actualizarEstadoMatricula);
    }

    @Override
    @Transactional(readOnly = true)
    public ComprobanteResponse comprobante(Long idPago) {
        Pago pago = repository.findById(idPago)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago", idPago));

        return new ComprobanteResponse(
                pago.getComprobante(),
                pago.getFecha(),
                nombreAlumno(pago.getMatriculaId()),
                nombrePaquete(pago.getMatriculaId()),
                pago.getMonto(),
                pago.getMetodo(),
                pago.getMatriculaId());
    }

    /** Precio vigente de la matrícula: el final con promoción o, si no hay, el del paquete. */
    private BigDecimal precioMatricula(Matricula matricula) {
        if (matricula.getPrecioFinal() != null) {
            return matricula.getPrecioFinal();
        }
        return paqueteRepository.findById(matricula.getPaqueteId())
                .map(Paquete::getPrecio)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal totalPagado(Long matriculaId) {
        BigDecimal total = repository.sumarPorMatricula(matriculaId);
        return total == null ? BigDecimal.ZERO : total;
    }

    private BigDecimal saldoPendiente(Matricula matricula) {
        return precioMatricula(matricula).subtract(totalPagado(matricula.getId()))
                .max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** Confirma la matrícula cuando el saldo llega a cero; la devuelve a POR_PAGAR si no. */
    private void actualizarEstadoMatricula(Matricula matricula) {
        if (matricula.getEstado() == EstadoMatricula.CANCELADA) {
            return;
        }
        matricula.setEstado(saldoPendiente(matricula).compareTo(BigDecimal.ZERO) == 0
                ? EstadoMatricula.CONFIRMADA
                : EstadoMatricula.POR_PAGAR);
        matriculaRepository.save(matricula);
    }

    private void validarMonto(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ReglaNegocioException("El monto del pago debe ser mayor a 0");
        }
    }

    private String normalizarMetodo(String metodo) {
        try {
            return MetodoPago.valueOf(metodo.trim().toUpperCase(Locale.ROOT)).name();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new ReglaNegocioException("El método de pago debe ser EFECTIVO, YAPE, PLIN o TRANSFERENCIA");
        }
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

    private String nombreAlumno(Long matriculaId) {
        return matriculaRepository.findById(matriculaId)
                .map(Matricula::getAlumnoId)
                .flatMap(alumnoRepository::findById)
                .map(alumno -> alumno.getNombre() + " " + alumno.getApellidos())
                .orElse("");
    }

    private String nombrePaquete(Long matriculaId) {
        return matriculaRepository.findById(matriculaId)
                .map(Matricula::getPaqueteId)
                .flatMap(paqueteRepository::findById)
                .map(Paquete::getNombre)
                .orElse("");
    }

    private PagoResponse aRespuesta(Pago pago) {
        Matricula matricula = matriculaRepository.findById(pago.getMatriculaId()).orElse(null);
        BigDecimal saldo = matricula == null ? BigDecimal.ZERO : saldoPendiente(matricula);
        String tipo = pago.getTipoPago() == null
                ? (saldo.compareTo(BigDecimal.ZERO) == 0 ? TipoPago.TOTAL.name() : TipoPago.FRACCIONADO.name())
                : pago.getTipoPago().name();

        return new PagoResponse(
                pago.getId(),
                pago.getMatriculaId(),
                nombreAlumno(pago.getMatriculaId()),
                pago.getMonto(),
                pago.getMetodo(),
                pago.getComprobante(),
                pago.getFecha(),
                saldo.setScale(2, RoundingMode.HALF_UP),
                tipo);
    }
}
