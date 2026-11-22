package com.sindaco.patrones.mvc;

import java.util.List;

/**
 * VISTA del patrón MVC. Se encarga únicamente de presentar la información;
 * no consulta la base de datos ni contiene lógica de negocio.
 */
public class VistaAlumnos {

    public String renderizar(List<ModeloAlumno> alumnos) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body><h1>Alumnos SINDACO</h1><ul>");
        for (ModeloAlumno alumno : alumnos) {
            html.append("<li>")
                    .append(alumno.nombreCompleto())
                    .append(" - DNI ")
                    .append(alumno.getDatos().dni())
                    .append("</li>");
        }
        return html.append("</ul></body></html>").toString();
    }

    public String mostrarError(String mensaje) {
        return "<html><body><h2>Error</h2><p>" + mensaje + "</p></body></html>";
    }
}
