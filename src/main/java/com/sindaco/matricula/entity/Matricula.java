package com.sindaco.matricula.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sindaco.matricula.enums.EstadoMatricula;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Matrícula de un alumno en un paquete de clases (CU01). */
@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;

    @Column(name = "paquete_id", nullable = false)
    private Long paqueteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoMatricula estado = EstadoMatricula.POR_PAGAR;

    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();

    /** Precio vigente de la matrícula después de aplicar la promoción (CU08). */
    @Column(name = "precio_final", precision = 12, scale = 2)
    private BigDecimal precioFinal;

    /** Descuento aplicado en moneda, no en porcentaje (CU08). */
    @Column(precision = 12, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    /** Código de la promoción aplicada, si la hubo (CU08). */
    @Column(name = "codigo_promocion", length = 30)
    private String codigoPromocion;

    public Matricula() {
    }

    public Matricula(Long alumnoId, Long paqueteId, EstadoMatricula estado, LocalDate fecha) {
        this.alumnoId = alumnoId;
        this.paqueteId = paqueteId;
        this.estado = estado;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlumnoId() {
        return alumnoId;
    }

    public void setAlumnoId(Long alumnoId) {
        this.alumnoId = alumnoId;
    }

    public Long getPaqueteId() {
        return paqueteId;
    }

    public void setPaqueteId(Long paqueteId) {
        this.paqueteId = paqueteId;
    }

    public EstadoMatricula getEstado() {
        return estado;
    }

    public void setEstado(EstadoMatricula estado) {
        this.estado = estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getPrecioFinal() {
        return precioFinal;
    }

    public void setPrecioFinal(BigDecimal precioFinal) {
        this.precioFinal = precioFinal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public String getCodigoPromocion() {
        return codigoPromocion;
    }

    public void setCodigoPromocion(String codigoPromocion) {
        this.codigoPromocion = codigoPromocion;
    }
}
