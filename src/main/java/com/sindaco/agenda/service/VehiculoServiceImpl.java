package com.sindaco.agenda.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.agenda.dto.VehiculoRequest;
import com.sindaco.agenda.dto.VehiculoResponse;
import com.sindaco.agenda.entity.Vehiculo;
import com.sindaco.agenda.enums.EstadoVehiculo;
import com.sindaco.agenda.repository.VehiculoRepository;
import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;

/** Implementación de los casos de uso de vehículos (CU04). */
@Service
@Transactional
public class VehiculoServiceImpl implements VehiculoService {

    private final VehiculoRepository repository;

    public VehiculoServiceImpl(VehiculoRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoResponse> listar() {
        return repository.findAll().stream().map(this::mapear).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoResponse buscarPorId(Long id) {
        return mapear(buscarEntidad(id));
    }

    @Override
    public VehiculoResponse crear(VehiculoRequest request) {
        Vehiculo vehiculo = new Vehiculo(
                request.placa(),
                request.marca(),
                request.modelo(),
                parsearEstado(request.estado()));
        return mapear(repository.save(vehiculo));
    }

    @Override
    public VehiculoResponse actualizar(Long id, VehiculoRequest request) {
        Vehiculo vehiculo = buscarEntidad(id);
        vehiculo.setPlaca(request.placa());
        vehiculo.setMarca(request.marca());
        vehiculo.setModelo(request.modelo());
        vehiculo.setEstado(parsearEstado(request.estado()));
        return mapear(repository.save(vehiculo));
    }

    @Override
    public void eliminar(Long id) {
        repository.delete(buscarEntidad(id));
    }

    @Override
    public VehiculoResponse cambiarEstado(Long id, EstadoVehiculo estado) {
        if (estado == null) {
            throw new ReglaNegocioException("El estado del vehículo es obligatorio");
        }
        Vehiculo vehiculo = buscarEntidad(id);
        vehiculo.setEstado(estado);
        return mapear(repository.save(vehiculo));
    }

    private Vehiculo buscarEntidad(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo", id));
    }

    /** El estado es opcional; por omisión el vehículo queda OPERATIVO. */
    private EstadoVehiculo parsearEstado(String estado) {
        if (estado == null || estado.isBlank()) {
            return EstadoVehiculo.OPERATIVO;
        }
        try {
            return EstadoVehiculo.valueOf(estado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ReglaNegocioException("Estado de vehículo inválido: " + estado);
        }
    }

    private VehiculoResponse mapear(Vehiculo vehiculo) {
        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getEstado());
    }
}
