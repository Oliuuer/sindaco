package com.sindaco.pago.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PagoResponse {
    private Long id;
    private Long matriculaId;
    private BigDecimal monto;
    private String concepto;
    private LocalDate fecha;

    public PagoResponse(Long id, Long matriculaId, BigDecimal monto, String concepto, LocalDate fecha) {
        this.id = id;
        this.matriculaId = matriculaId;
        this.monto = monto;
        this.concepto = concepto;
        this.fecha = fecha;
    }

    public Long getId() {
        return id;
    }

    public Long getMatriculaId() {
        return matriculaId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public LocalDate getFecha() {
        return fecha;
    }
}
