package com.sindaco.pago.service;

import com.sindaco.pago.dto.PagoRequest;
import com.sindaco.pago.dto.PagoResponse;

import java.util.List;

public interface PagoService {
    PagoResponse crear(PagoRequest request);
    List<PagoResponse> listar();
}
