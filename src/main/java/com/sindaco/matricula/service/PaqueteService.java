package com.sindaco.matricula.service;

import java.util.List;

import com.sindaco.matricula.dto.PaqueteRequest;
import com.sindaco.matricula.dto.PaqueteResponse;

/** Casos de uso de paquetes y promociones (CU08). */
public interface PaqueteService {

    List<PaqueteResponse> listar(boolean soloVigentes);

    PaqueteResponse obtener(Long id);

    PaqueteResponse crear(PaqueteRequest request);

    PaqueteResponse actualizar(Long id, PaqueteRequest request);

    void eliminar(Long id);
}
