package com.sindaco.comun.exception;

/** Conflicto con el estado actual del sistema ( horarios ocupados, datos duplicados ). HTTP 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
