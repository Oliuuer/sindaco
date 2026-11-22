package com.sindaco.matricula.promocion.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sindaco.matricula.promocion.entity.Promocion;

/** Acceso a datos de las promociones. */
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    /** Busca una promoción por su código único. */
    Optional<Promocion> findByCodigo(String codigo);

    /** Lista las promociones vigentes ordenadas por la fecha de fin más próxima. */
    List<Promocion> findByVigenteTrueOrderByFechaFinAsc();
}
