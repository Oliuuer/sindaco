package com.sindaco.matricula.dto;

import java.math.BigDecimal;

public record PaqueteResponse(
        Long id,
        String nombre,
        String categoria,
        Integer numeroClases,
        BigDecimal precio,
        boolean vigente) {
}
