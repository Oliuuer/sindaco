package com.sindaco.mantenimiento.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.agenda.service.VehiculoService;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.mantenimiento.dto.MantenimientoRequest;
import com.sindaco.mantenimiento.dto.MantenimientoResponse;
import com.sindaco.mantenimiento.entity.Mantenimiento;
import com.sindaco.mantenimiento.enums.EstadoMantenimiento;
import com.sindaco.mantenimiento.repository.MantenimientoRepository;

/** Implementación de la gestión de mantenimiento de vehículos (CU07). */
@Service
@Transactional(readOnly = true)
public class MantenimientoServiceImpl implements MantenimientoService {

    private final MantenimientoRepository repository;
    private final VehiculoService vehiculoService;

    public MantenimientoServiceImpl(MantenimientoRepository repository, VehiculoService vehiculoService) {
        this.repository = repository;
        this.vehiculoService = vehiculoService;
    }

    @Override
    public List<MantenimientoResponse> listar() {
        return repository.findAll().stream().map(this::aRespuesta).toList();
    }

    @Override
    public MantenimientoResponse buscarPorId(Long id) {
        return repository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mantenimiento", id));
    }

    @Override
    @Transactional
    public MantenimientoResponse crear(MantenimientoRequest request) {
        validar(request);
        vehiculoService.buscarPorId(request.vehiculoId());

        Mantenimiento mantenimiento = new Mantenimiento();
        aplicar(mantenimiento, request, LocalDate.now());
        return aRespuesta(repository.save(mantenimiento));
    }

    @Override
    @Transactional
    public MantenimientoResponse actualizar(Long id, MantenimientoRequest request) {
        Mantenimiento mantenimiento = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mantenimiento", id));
        validar(request);
        vehiculoService.buscarPorId(request.vehiculoId());

        aplicar(mantenimiento, request, mantenimiento.getFecha());
        return aRespuesta(repository.save(mantenimiento));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Mantenimiento mantenimiento = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mantenimiento", id));
        repository.delete(mantenimiento);
    }

    private void validar(MantenimientoRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("Los datos del mantenimiento son obligatorios");
        }
        if (request.vehiculoId() == null) {
            throw new ReglaNegocioException("El id del vehículo es obligatorio");
        }
        if (request.tipo() == null) {
            throw new ReglaNegocioException("El tipo de mantenimiento es obligatorio");
        }
        if (request.descripcion() == null || request.descripcion().isBlank()) {
            throw new ReglaNegocioException("La descripción del mantenimiento es obligatoria");
        }
        if (request.proveedor() == null || request.proveedor().isBlank()) {
            throw new ReglaNegocioException("El proveedor es obligatorio");
        }
    }

    private void aplicar(Mantenimiento destino, MantenimientoRequest request, LocalDate fechaPorDefecto) {
        destino.setVehiculoId(request.vehiculoId());
        destino.setTipo(request.tipo());
        destino.setDescripcion(request.descripcion().trim());
        destino.setFecha(parsearFecha(request.fecha(), fechaPorDefecto));
        destino.setEstado(request.estado() == null ? EstadoMantenimiento.PENDIENTE : request.estado());
        destino.setProveedor(request.proveedor().trim());
        destino.setCosto(request.costo() == null ? BigDecimal.ZERO : request.costo());
    }

    private LocalDate parsearFecha(String valor, LocalDate porDefecto) {
        if (valor == null || valor.isBlank()) {
            return porDefecto;
        }
        try {
            return LocalDate.parse(valor.trim());
        } catch (DateTimeParseException ex) {
            throw new ReglaNegocioException("El formato de la fecha debe ser yyyy-MM-dd");
        }
    }

    private MantenimientoResponse aRespuesta(Mantenimiento m) {
        return new MantenimientoResponse(
                m.getId(),
                m.getVehiculoId(),
                placaVehiculo(m.getVehiculoId()),
                m.getTipo(),
                m.getDescripcion(),
                m.getFecha(),
                m.getEstado(),
                m.getProveedor(),
                m.getCosto());
    }

    private String placaVehiculo(Long vehiculoId) {
        if (vehiculoId == null) {
            return "";
        }
        try {
            return texto(vehiculoService.buscarPorId(vehiculoId).placa());
        } catch (RecursoNoEncontradoException ex) {
            return "";
        }
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }
}
