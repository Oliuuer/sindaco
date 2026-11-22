package com.sindaco.comun.notificacion.dto;

import java.time.LocalDateTime;

/** Salida con los datos de una notificación. */
public record NotificacionResponse(
        Long id,
        String tipo,
        String titulo,
        String mensaje,
        LocalDateTime fecha,
        String destinatarioRol,
        Long alumnoId,
        boolean leida) {
}
