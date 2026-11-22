package com.sindaco.matricula.dto;

import java.time.LocalDate;

import com.sindaco.matricula.enums.EstadoMatricula;

/** Datos de una matrícula que se devuelven al frontend (CU01). */
public record MatriculaResponse(
        Long id,
        Long alumnoId,
        String alumnoNombre,
        Long paqueteId,
        String paquete,
        EstadoMatricula estado,
        LocalDate fecha) {
}
