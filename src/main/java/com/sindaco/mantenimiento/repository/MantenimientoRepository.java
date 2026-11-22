package com.sindaco.mantenimiento.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sindaco.mantenimiento.entity.Mantenimiento;
import com.sindaco.mantenimiento.enums.EstadoMantenimiento;

/** Acceso a datos de mantenimientos (CU07). */
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {

    List<Mantenimiento> findByVehiculoId(Long vehiculoId);

    List<Mantenimiento> findByVehiculoIdOrderByFechaDesc(Long vehiculoId);

    @Query("select m from Mantenimiento m "
            + "where m.vehiculoId = :vehiculoId and m.estado = :estado "
            + "order by m.fecha desc")
    List<Mantenimiento> findByVehiculoIdYEstado(@Param("vehiculoId") Long vehiculoId,
            @Param("estado") EstadoMantenimiento estado);

    @Query("select count(m) from Mantenimiento m where m.estado = :estado")
    long contarPorEstado(@Param("estado") EstadoMantenimiento estado);
}
