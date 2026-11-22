package com.sindaco.comun.notificacion.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Notificación emitida por el sistema para un rol o un alumno concreto. */
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(name = "destinatario_rol", length = 30)
    private String destinatarioRol;

    @Column(name = "alumno_id")
    private Long alumnoId;

    @Column(nullable = false)
    private boolean leida = false;

    public Notificacion() {
    }

    public Notificacion(Long id, String tipo, String titulo, String mensaje, LocalDateTime fecha,
            String destinatarioRol, Long alumnoId, boolean leida) {
        this.id = id;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.fecha = fecha;
        this.destinatarioRol = destinatarioRol;
        this.alumnoId = alumnoId;
        this.leida = leida;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDestinatarioRol() {
        return destinatarioRol;
    }

    public void setDestinatarioRol(String destinatarioRol) {
        this.destinatarioRol = destinatarioRol;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }
}
