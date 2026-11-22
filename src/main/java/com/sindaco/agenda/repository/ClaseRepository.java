package com.sindaco.agenda.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sindaco.agenda.entity.Clase;

/** Acceso a datos de clases y consultas de disponibilidad de agenda (CU03). */
public interface ClaseRepository extends JpaRepository<Clase, Long> {

    List<Clase> findByInstructorId(Long instructorId);

    /** Horarios de un alumno, de la fecha más próxima a la más lejana. */
    List<Clase> findByAlumnoIdOrderByFechaAscHoraInicioAsc(Long alumnoId);

    List<Clase> findByVehiculoId(Long vehiculoId);

    List<Clase> findByFecha(LocalDate fecha);

    /**
     * Cuenta las clases de una fecha que se solapan con el intervalo indicado
     * cuando coinciden el instructor o el vehículo. Un intervalo se solapa si
     * horaInicio &lt; horaFin existente y horaFin &gt; horaInicio existente.
     * El id se excluye para que una clase no entre en conflicto consigo misma
     * durante una actualización.
     */
    @Query("""
            SELECT COUNT(c) FROM Clase c
            WHERE c.id <> :id
              AND c.fecha = :fecha
              AND ((c.instructorId = :instructorId
                      AND c.horaInicio < :horaFin AND c.horaFin > :horaInicio)
                OR (c.vehiculoId = :vehiculoId
                      AND c.horaInicio < :horaFin AND c.horaFin > :horaInicio))
            """)
    long contarSolapamientos(@Param("id") Long id,
                             @Param("instructorId") Long instructorId,
                             @Param("vehiculoId") Long vehiculoId,
                             @Param("fecha") LocalDate fecha,
                             @Param("horaInicio") LocalTime horaInicio,
                             @Param("horaFin") LocalTime horaFin);

    /**
     * Método de comodidad para detectar solapamientos. Al crear no hay id todavía,
     * por lo que se usa -1L (un id inexistente) como valor de exclusión.
     */
    default boolean existeSolapamiento(Long id, Long instructorId, Long vehiculoId,
                                       LocalDate fecha, LocalTime horaInicio, LocalTime horaFin) {
        return contarSolapamientos(id == null ? -1L : id, instructorId, vehiculoId,
                fecha, horaInicio, horaFin) > 0;
    }
}
