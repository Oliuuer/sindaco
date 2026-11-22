package com.sindaco.agenda.service;

import java.util.List;

import com.sindaco.agenda.dto.VehiculoRequest;
import com.sindaco.agenda.dto.VehiculoResponse;
import com.sindaco.agenda.enums.EstadoVehiculo;

/** Casos de uso de vehículos (CU04). */
public interface VehiculoService {

    List<VehiculoResponse> listar();

    VehiculoResponse buscarPorId(Long id);

    VehiculoResponse crear(VehiculoRequest request);

    VehiculoResponse actualizar(Long id, VehiculoRequest request);

    void eliminar(Long id);

    /** Cambia el estado de un vehículo; lo usa mantenimiento (CU07) al iniciar o finalizar un servicio. */
    VehiculoResponse cambiarEstado(Long id, EstadoVehiculo estado);
}
