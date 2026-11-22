package com.sindaco.patrones.mvc;

import com.sindaco.patrones.dao.AlumnoDto;

/**
 * MODELO del patrón MVC. Contiene el estado del alumno y las reglas simples
 * que le corresponden, independientes de la vista y del controlador.
 */
public class ModeloAlumno {

    private final AlumnoDto datos;

    public ModeloAlumno(AlumnoDto datos) {
        this.datos = datos;
    }

    public String nombreCompleto() {
        return datos.nombre() + " " + datos.apellidos();
    }

    public boolean dniValido() {
        return datos.dni() != null && datos.dni().matches("\\d{8}");
    }

    public AlumnoDto getDatos() {
        return datos;
    }
}
