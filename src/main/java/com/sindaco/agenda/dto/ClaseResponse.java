package com.sindaco.agenda.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ClaseResponse {
    private Long id;
    private Long instructorId;
    private Long alumnoId;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;

    public ClaseResponse(Long id, Long instructorId, Long alumnoId, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, String estado) {
        this.id = id;
        this.instructorId = instructorId;
        this.alumnoId = alumnoId;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public Long getInstructorId() {
        return instructorId;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public String getEstado() {
        return estado;
    }
}
