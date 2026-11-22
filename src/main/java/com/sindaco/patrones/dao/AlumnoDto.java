package com.sindaco.patrones.dao;

/**
 * DTO del patrón DAO. Transporta los datos del alumno entre la base de datos,
 * el DAO y el cliente, sin exponer la entidad JPA.
 */
public record AlumnoDto(
        Long id,
        String nombre,
        String apellidos,
        String dni,
        String email,
        String telefono) {
}
