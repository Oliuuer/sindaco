package com.sindaco.matricula.service;

import java.time.LocalDate;
import java.util.List;

import com.sindaco.matricula.dto.ComprobanteResponse;
import com.sindaco.matricula.dto.PagoRequest;
import com.sindaco.matricula.dto.PagoResponse;

/** Casos de uso de pagos y comprobantes (CU02). */
public interface PagoService {

    List<PagoResponse> listar();

    PagoResponse buscarPorId(Long id);

    PagoResponse crear(PagoRequest request);

    void eliminar(Long id);

    ComprobanteResponse comprobante(Long idPago);

    List<PagoResponse> listarPorMatricula(Long matriculaId);

    List<PagoResponse> listarPorFecha(LocalDate desde, LocalDate hasta);
}
