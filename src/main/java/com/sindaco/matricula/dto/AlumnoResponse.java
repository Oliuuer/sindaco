package com.sindaco.matricula.dto;

import java.time.LocalDate;

/** Datos de un alumno que se devuelven al frontend (CU01). */
public record AlumnoResponse(
        Long id,
        String nombre,
        String apellidos,
        String dni,
        String email,
        String telefono,
        String direccion,
        LocalDate fechaRegistro) {
}
