package com.sindaco.matricula.dto;

import com.sindaco.matricula.enums.EstadoMatricula;
import java.time.LocalDate;

public class MatriculaResponse {
    private Long id;
    private Long alumnoId;
    private String paquete;
    private EstadoMatricula estado;
    private LocalDate fecha;

    public MatriculaResponse(Long id, Long alumnoId, String paquete, EstadoMatricula estado, LocalDate fecha) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.paquete = paquete;
        this.estado = estado;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public String getPaquete() {
        return paquete;
    }

    public EstadoMatricula getEstado() {
        return estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
