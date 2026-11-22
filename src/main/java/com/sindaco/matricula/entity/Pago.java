package com.sindaco.matricula.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sindaco.matricula.enums.TipoPago;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Pago registrado para una matrícula (CU02). */
@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "matricula_id", nullable = false)
    private Long matriculaId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 20)
    private String metodo;

    /** Se asigna automáticamente (B-000001) cuando el usuario no envía uno. */
    @Column(unique = true, length = 50)
    private String comprobante;

    @Column(nullable = false)
    private LocalDate fecha = LocalDate.now();

    /**
     * TOTAL cuando cancela el saldo; FRACCIONADO cuando es un abono.
     * La columna es nullable y tiene valor por defecto para que la migración
     * automática de Hibernate funcione aunque la tabla ya tenga pagos antiguos.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", length = 20)
    @ColumnDefault("'TOTAL'")
    private TipoPago tipoPago = TipoPago.TOTAL;

    public Pago() {
    }

    public Pago(Long matriculaId, BigDecimal monto, String metodo, String comprobante, LocalDate fecha) {
        this(matriculaId, monto, metodo, comprobante, fecha, TipoPago.TOTAL);
    }

    public Pago(Long matriculaId, BigDecimal monto, String metodo, String comprobante, LocalDate fecha,
                TipoPago tipoPago) {
        this.matriculaId = matriculaId;
        this.monto = monto;
        this.metodo = metodo;
        this.comprobante = comprobante;
        this.fecha = fecha;
        this.tipoPago = tipoPago == null ? TipoPago.TOTAL : tipoPago;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatriculaId() {
        return matriculaId;
    }

    public void setMatriculaId(Long matriculaId) {
        this.matriculaId = matriculaId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getComprobante() {
        return comprobante;
    }

    public void setComprobante(String comprobante) {
        this.comprobante = comprobante;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public TipoPago getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(TipoPago tipoPago) {
        this.tipoPago = tipoPago;
    }
}
