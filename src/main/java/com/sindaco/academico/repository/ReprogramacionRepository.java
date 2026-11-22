package com.sindaco.academico.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sindaco.academico.entity.Reprogramacion;
import com.sindaco.academico.enums.EstadoReprogramacion;

/** Acceso a datos de reprogramaciones (CU09). */
public interface ReprogramacionRepository extends JpaRepository<Reprogramacion, Long> {

    List<Reprogramacion> findByAlumnoId(Long alumnoId);

    List<Reprogramacion> findByAlumnoIdOrderByFechaDesc(Long alumnoId);

    List<Reprogramacion> findByClaseId(Long claseId);

    List<Reprogramacion> findByClaseIdOrderByFechaDesc(Long claseId);

    @Query("select r from Reprogramacion r "
            + "where r.alumnoId = :alumnoId and r.estado = :estado "
            + "order by r.fecha desc")
    List<Reprogramacion> findByAlumnoIdYEstado(@Param("alumnoId") Long alumnoId,
            @Param("estado") EstadoReprogramacion estado);
}
