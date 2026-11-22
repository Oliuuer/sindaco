package com.sindaco.comun.notificacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Entrada para registrar una nueva notificación. */
public record NotificacionRequest(
        @NotBlank(message = "El tipo de notificación es obligatorio") String tipo,
        @NotBlank(message = "El título es obligatorio") String titulo,
        @NotBlank(message = "El mensaje es obligatorio")
        @Size(max = 500, message = "El mensaje no puede superar 500 caracteres") String mensaje,
        String destinatarioRol,
        Long alumnoId) {
}
