package com.sindaco.agenda.service;

import java.util.List;

import com.sindaco.agenda.dto.VehiculoRequest;
import com.sindaco.agenda.dto.VehiculoResponse;

/** Casos de uso de vehículos (CU04). */
public interface VehiculoService {

    List<VehiculoResponse> listar();

    VehiculoResponse buscarPorId(Long id);

    VehiculoResponse crear(VehiculoRequest request);

    VehiculoResponse actualizar(Long id, VehiculoRequest request);

    void eliminar(Long id);
}
