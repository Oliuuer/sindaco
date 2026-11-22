package com.sindaco.academico.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.sindaco.academico.enums.EstadoReprogramacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Reprogramación o justificación de ausencia de un alumno (CU09). */
@Entity
@Table(name = "reprogramaciones")
public class Reprogramacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;

    @Column(name = "clase_id", nullable = false)
    private Long claseId;

    @Column(nullable = false, length = 500)
    private String motivo;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "nueva_fecha")
    private LocalDate nuevaFecha;

    @Column(name = "nueva_hora_inicio")
    private LocalTime nuevaHoraInicio;

    @Column(name = "nueva_hora_fin")
    private LocalTime nuevaHoraFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private EstadoReprogramacion estado;

    public Reprogramacion() {
    }

    public Long getId() {
        return id;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public Long getClaseId() {
        return claseId;
    }

    public void setClaseId(Long claseId) {
        this.claseId = claseId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getNuevaFecha() {
        return nuevaFecha;
    }

    public void setNuevaFecha(LocalDate nuevaFecha) {
        this.nuevaFecha = nuevaFecha;
    }

    public LocalTime getNuevaHoraInicio() {
        return nuevaHoraInicio;
    }

    public void setNuevaHoraInicio(LocalTime nuevaHoraInicio) {
        this.nuevaHoraInicio = nuevaHoraInicio;
    }

    public LocalTime getNuevaHoraFin() {
        return nuevaHoraFin;
    }

    public void setNuevaHoraFin(LocalTime nuevaHoraFin) {
        this.nuevaHoraFin = nuevaHoraFin;
    }

    public EstadoReprogramacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoReprogramacion estado) {
        this.estado = estado;
    }
}
