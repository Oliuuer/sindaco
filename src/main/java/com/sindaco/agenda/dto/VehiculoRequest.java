package com.sindaco.agenda.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Datos de entrada para registrar o actualizar un vehículo (CU04). */
public record VehiculoRequest(

        @NotBlank(message = "La placa es obligatoria")
        @Pattern(regexp = "^[A-Za-z0-9]{3}[-\\s]?[A-Za-z0-9]{3,4}$",
                message = "La placa no es válida (formato esperado: ABC-123)")
        String placa,

        @NotBlank(message = "La marca es obligatoria")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        String modelo,

        String estado) {
}
