package com.sindaco.matricula.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PaqueteRequest(
        @NotBlank String nombre,
        @NotBlank String categoria,
        @NotNull @Positive Integer numeroClases,
        @NotNull @DecimalMin("0.0") BigDecimal precio) {
}
