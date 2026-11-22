package com.sindaco.agenda.dto;

import com.sindaco.agenda.enums.EstadoVehiculo;

/** Datos de salida de un vehículo (CU04). */
public record VehiculoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        EstadoVehiculo estado) {
}
