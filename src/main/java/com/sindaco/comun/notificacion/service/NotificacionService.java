package com.sindaco.comun.notificacion.service;

import java.util.List;

import com.sindaco.comun.notificacion.dto.NotificacionRequest;
import com.sindaco.comun.notificacion.dto.NotificacionResponse;

/** Casos de uso de las notificaciones del sistema. */
public interface NotificacionService {

    /** Devuelve todas las notificaciones, de la más reciente a la más antigua. */
    List<NotificacionResponse> listar();

    /** Devuelve las notificaciones dirigidas a un rol. */
    List<NotificacionResponse> listarPorRol(String rol);

    /** Devuelve las notificaciones dirigidas a un alumno. */
    List<NotificacionResponse> listarPorAlumno(Long alumnoId);

    /** Registra una notificación a partir de sus datos sueltos. */
    NotificacionResponse registrar(String tipo, String titulo, String mensaje, String destinatarioRol,
            Long alumnoId);

    /** Registra una notificación a partir de la solicitud recibida. */
    NotificacionResponse crear(NotificacionRequest request);

    /** Marca como leída la notificación indicada. */
    void marcarLeida(Long id);
}
