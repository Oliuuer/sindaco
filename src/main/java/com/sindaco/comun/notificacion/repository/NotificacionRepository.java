package com.sindaco.comun.notificacion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sindaco.comun.notificacion.entity.Notificacion;

/** Acceso a datos de las notificaciones del sistema. */
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findAllByOrderByFechaDesc();

    List<Notificacion> findByDestinatarioRolOrderByFechaDesc(String destinatarioRol);

    List<Notificacion> findByAlumnoIdOrderByFechaDesc(Long alumnoId);
}
