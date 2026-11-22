package com.sindaco.comun.exception;

/** Violación de una regla de negocio o de validación de datos. Se traduce a HTTP 400. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
