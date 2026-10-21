package com.sindaco.matricula.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sindaco.matricula.entity.Paquete;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {

    /** Consulta JPQL: trabaja con la clase Paquete y sus atributos, no con tablas. */
    @Query("SELECT p FROM Paquete p WHERE p.vigente = true ORDER BY p.precio ASC")
    List<Paquete> findVigentes();
}
