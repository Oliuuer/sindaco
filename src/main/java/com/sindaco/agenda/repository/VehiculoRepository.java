package com.sindaco.agenda.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sindaco.agenda.entity.Vehiculo;
import com.sindaco.agenda.enums.EstadoVehiculo;

/** Acceso a datos de vehículos (CU04). */
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    /** Vehículos filtrados por estado (por ejemplo, solo OPERATIVO). */
    List<Vehiculo> findByEstado(EstadoVehiculo estado);
}
