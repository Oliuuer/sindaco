package com.sindaco.matricula.promocion.service;

import java.math.BigDecimal;
import java.util.List;

import com.sindaco.matricula.promocion.dto.PromocionRequest;
import com.sindaco.matricula.promocion.dto.PromocionResponse;

/** Casos de uso del módulo de promociones. */
public interface PromocionService {

    /** Lista las promociones vigentes ordenadas por fecha de fin. */
    List<PromocionResponse> listar();

    /** Busca una promoción por su identificador. */
    PromocionResponse buscarPorId(Long id);

    /** Crea una promoción nueva. */
    PromocionResponse crear(PromocionRequest request);

    /** Actualiza una promoción existente. */
    PromocionResponse actualizar(Long id, PromocionRequest request);

    /** Elimina una promoción por su identificador. */
    void eliminar(Long id);

    /** Busca una promoción por código verificando que esté vigente en la fecha actual. */
    PromocionResponse buscarVigentePorCodigo(String codigo);

    /** Aplica el descuento de la promoción indicada a un precio. */
    BigDecimal aplicarDescuento(BigDecimal precio, String codigo);
}
