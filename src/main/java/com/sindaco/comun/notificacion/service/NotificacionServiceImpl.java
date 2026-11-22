package com.sindaco.comun.notificacion.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.comun.notificacion.dto.NotificacionRequest;
import com.sindaco.comun.notificacion.dto.NotificacionResponse;
import com.sindaco.comun.notificacion.entity.Notificacion;
import com.sindaco.comun.notificacion.repository.NotificacionRepository;

/** Implementación de la gestión de notificaciones del sistema. */
@Service
@Transactional(readOnly = true)
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository repository;

    public NotificacionServiceImpl(NotificacionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<NotificacionResponse> listar() {
        return repository.findAllByOrderByFechaDesc().stream().map(this::mapear).toList();
    }

    @Override
    public List<NotificacionResponse> listarPorRol(String rol) {
        return repository.findByDestinatarioRolOrderByFechaDesc(rol).stream().map(this::mapear).toList();
    }

    @Override
    public List<NotificacionResponse> listarPorAlumno(Long alumnoId) {
        return repository.findByAlumnoIdOrderByFechaDesc(alumnoId).stream().map(this::mapear).toList();
    }

    @Override
    @Transactional
    public NotificacionResponse registrar(String tipo, String titulo, String mensaje, String destinatarioRol,
            Long alumnoId) {
        Notificacion notificacion = new Notificacion();
        notificacion.setTipo(tipo);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setFecha(LocalDateTime.now());
        notificacion.setDestinatarioRol(destinatarioRol);
        notificacion.setAlumnoId(alumnoId);
        notificacion.setLeida(false);
        return mapear(repository.save(notificacion));
    }

    @Override
    @Transactional
    public NotificacionResponse crear(NotificacionRequest request) {
        if (request == null) {
            throw new ReglaNegocioException("Los datos de la notificación son obligatorios");
        }
        return registrar(request.tipo(), request.titulo(), request.mensaje(), request.destinatarioRol(),
                request.alumnoId());
    }

    @Override
    @Transactional
    public void marcarLeida(Long id) {
        Notificacion notificacion = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación", id));
        notificacion.setLeida(true);
        repository.save(notificacion);
    }

    private NotificacionResponse mapear(Notificacion notificacion) {
        return new NotificacionResponse(
                notificacion.getId(),
                notificacion.getTipo(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getFecha(),
                notificacion.getDestinatarioRol(),
                notificacion.getAlumnoId(),
                notificacion.isLeida());
    }
}
