package com.sindaco.patrones.mvc;

import java.util.List;

import com.sindaco.patrones.dao.AlumnoDao;

/**
 * CONTROLADOR del patrón MVC. Recibe la petición, obtiene los datos mediante
 * el DAO, construye el modelo y delega la presentación a la vista.
 */
public class ControladorAlumnos {

    private final AlumnoDao dao;
    private final VistaAlumnos vista;

    public ControladorAlumnos(AlumnoDao dao, VistaAlumnos vista) {
        this.dao = dao;
        this.vista = vista;
    }

    public String listar() {
        List<ModeloAlumno> modelos = dao.listar().stream()
                .map(ModeloAlumno::new)
                .toList();
        return vista.renderizar(modelos);
    }

    public String buscar(Long id) {
        return dao.buscarPorId(id)
                .map(datos -> vista.renderizar(List.of(new ModeloAlumno(datos))))
                .orElseGet(() -> vista.mostrarError("No existe el alumno con id " + id));
    }
}
