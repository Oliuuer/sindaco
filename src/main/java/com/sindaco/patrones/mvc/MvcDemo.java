package com.sindaco.patrones.mvc;

import com.sindaco.patrones.dao.AlumnoDaoImpl;

/** Punto de entrada de la demostración MVC sobre datos reales de PostgreSQL. */
public class MvcDemo {

    public static void main(String[] args) {
        ControladorAlumnos controlador =
                new ControladorAlumnos(new AlumnoDaoImpl(), new VistaAlumnos());

        System.out.println("=== PATRÓN MVC - LISTADO ===");
        System.out.println(controlador.listar());

        System.out.println("\n=== PATRÓN MVC - BÚSQUEDA ===");
        System.out.println(controlador.buscar(1L));
    }
}
