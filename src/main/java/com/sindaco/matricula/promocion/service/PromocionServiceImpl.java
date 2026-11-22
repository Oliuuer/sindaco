package com.sindaco.matricula.promocion.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.comun.exception.ConflictoException;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.matricula.promocion.dto.PromocionRequest;
import com.sindaco.matricula.promocion.dto.PromocionResponse;
import com.sindaco.matricula.promocion.entity.Promocion;
import com.sindaco.matricula.promocion.repository.PromocionRepository;

/** Implementación de los casos de uso del módulo de promociones. */
@Service
@Transactional(readOnly = true)
public class PromocionServiceImpl implements PromocionService {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final PromocionRepository repository;

    public PromocionServiceImpl(PromocionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PromocionResponse> listar() {
        return repository.findByVigenteTrueOrderByFechaFinAsc().stream()
                .map(this::aRespuesta)
                .toList();
    }

    @Override
    public PromocionResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Promoción", id));
    }

    @Override
    @Transactional
    public PromocionResponse crear(PromocionRequest request) {
        String codigo = normalizarCodigo(request.codigo());
        validarFechas(request.fechaInicio(), request.fechaFin());
        validarCodigoUnico(codigo, null);

        Promocion promocion = new Promocion();
        aplicarRequest(promocion, request, codigo);
        return aRespuesta(repository.save(promocion));
    }

    @Override
    @Transactional
    public PromocionResponse actualizar(Long id, PromocionRequest request) {
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Promoción", id));
        String codigo = normalizarCodigo(request.codigo());
        validarFechas(request.fechaInicio(), request.fechaFin());
        validarCodigoUnico(codigo, id);

        aplicarRequest(promocion, request, codigo);
        return aRespuesta(repository.save(promocion));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Promocion promocion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Promoción", id));
        repository.delete(promocion);
    }

    @Override
    public PromocionResponse buscarVigentePorCodigo(String codigo) {
        String codigoNormalizado = normalizarCodigo(codigo);
        Promocion promocion = repository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No existe la promoción con código " + codigoNormalizado));
        validarVigencia(promocion);
        return aRespuesta(promocion);
    }

    @Override
    public BigDecimal aplicarDescuento(BigDecimal precio, String codigo) {
        if (precio == null) {
            throw new ReglaNegocioException("El precio es obligatorio para aplicar un descuento");
        }
        if (codigo == null || codigo.isBlank()) {
            return precio;
        }

        String codigoNormalizado = normalizarCodigo(codigo);
        Promocion promocion = repository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new ReglaNegocioException(
                        "No existe la promoción con código " + codigoNormalizado));
        validarVigencia(promocion);

        BigDecimal descuento = precio.multiply(promocion.getPorcentajeDescuento())
                .divide(CIEN, 10, RoundingMode.HALF_UP);
        return precio.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
    }

    /** Normaliza el código a mayúsculas y elimina espacios. */
    private String normalizarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioException("El código de la promoción es obligatorio");
        }
        return codigo.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    /** Verifica que el rango de fechas sea válido. */
    private void validarFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) {
            throw new ReglaNegocioException("Las fechas de la promoción son obligatorias");
        }
        if (fechaFin.isBefore(fechaInicio)) {
            throw new ReglaNegocioException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
    }

    /** Verifica que el código no esté ya asignado a otra promoción. */
    private void validarCodigoUnico(String codigo, Long idActual) {
        repository.findByCodigo(codigo).ifPresent(p -> {
            if (idActual == null || !idActual.equals(p.getId())) {
                throw new ConflictoException("Ya existe una promoción con el código " + codigo);
            }
        });
    }

    /** Verifica que la promoción esté activa dentro de su rango de fechas. */
    private void validarVigencia(Promocion promocion) {
        LocalDate hoy = LocalDate.now();
        if (!promocion.isVigente() || hoy.isBefore(promocion.getFechaInicio())
                || hoy.isAfter(promocion.getFechaFin())) {
            throw new ReglaNegocioException("La promoción " + promocion.getCodigo()
                    + " no está vigente en la fecha actual");
        }
    }

    /** Copia los datos de entrada a la entidad. */
    private void aplicarRequest(Promocion promocion, PromocionRequest request, String codigo) {
        promocion.setCodigo(codigo);
        promocion.setDescripcion(request.descripcion() == null ? null : request.descripcion().trim());
        promocion.setPorcentajeDescuento(request.porcentajeDescuento());
        promocion.setFechaInicio(request.fechaInicio());
        promocion.setFechaFin(request.fechaFin());
        if (request.vigente() != null) {
            promocion.setVigente(request.vigente());
        }
    }

    /** Convierte una entidad en su respuesta, calculando si está activa hoy. */
    private PromocionResponse aRespuesta(Promocion promocion) {
        LocalDate hoy = LocalDate.now();
        boolean activaHoy = promocion.isVigente()
                && !hoy.isBefore(promocion.getFechaInicio())
                && !hoy.isAfter(promocion.getFechaFin());
        return new PromocionResponse(
                promocion.getId(),
                promocion.getCodigo(),
                promocion.getDescripcion(),
                promocion.getPorcentajeDescuento(),
                promocion.getFechaInicio(),
                promocion.getFechaFin(),
                promocion.isVigente(),
                activaHoy);
    }
}
