package com.sindaco.agenda.service;

import java.util.List;

import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;

/** Casos de uso de la agenda de clases (CU03). */
public interface ClaseService {

    List<ClaseResponse> listar();

    ClaseResponse buscarPorId(Long id);

    ClaseResponse crear(ClaseRequest request);

    ClaseResponse actualizar(Long id, ClaseRequest request);

    void eliminar(Long id);

    /** Clases asignadas a un instructor; útil para otros módulos (académico, reportes). */
    List<ClaseResponse> listarPorInstructor(Long instructorId);

    /**
     * Reutiliza las reglas de disponibilidad de la agenda (instructor, vehículo
     * operativo y ausencia de solapamientos) sin Guardar cambios. La usa el módulo
     * de reprogramaciones (CU09) antes de mover una clase.
     *
     * @param idClase id de la clase que se está reprogramando, para excluirla del
     *                control de solapamientos
     */
    void validarDisponibilidad(ClaseRequest request, Long idClase);
}
