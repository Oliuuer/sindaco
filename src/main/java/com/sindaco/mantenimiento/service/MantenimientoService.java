package com.sindaco.mantenimiento.service;

import java.util.List;

import com.sindaco.mantenimiento.dto.MantenimientoRequest;
import com.sindaco.mantenimiento.dto.MantenimientoResponse;

/** Casos de uso de mantenimiento de vehículos (CU07). */
public interface MantenimientoService {

    List<MantenimientoResponse> listar();

    MantenimientoResponse buscarPorId(Long id);

    MantenimientoResponse crear(MantenimientoRequest request);

    MantenimientoResponse actualizar(Long id, MantenimientoRequest request);

    void eliminar(Long id);
}
