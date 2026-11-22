package com.sindaco.matricula.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sindaco.matricula.entity.Pago;

/** Acceso a datos de pagos (CU02 y reportes). */
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** Consulta JPQL: pagos asociados a una matrícula. */
    @Query("SELECT p FROM Pago p WHERE p.matriculaId = :matriculaId ORDER BY p.fecha DESC, p.id DESC")
    List<Pago> findByMatriculaId(@Param("matriculaId") Long matriculaId);

    /** Consulta JPQL: pagos en un rango de fechas, usada por el módulo de reportes. */
    @Query("SELECT p FROM Pago p WHERE p.fecha BETWEEN :desde AND :hasta ORDER BY p.fecha ASC, p.id ASC")
    List<Pago> findByFechaBetween(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("SELECT p FROM Pago p WHERE p.comprobante = :comprobante")
    Optional<Pago> findByComprobante(@Param("comprobante") String comprobante);

    /** Suma de todos los abonos de una matrícula; cero cuando todavía no tiene pagos. */
    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.matriculaId = :matriculaId")
    BigDecimal sumarPorMatricula(@Param("matriculaId") Long matriculaId);
}
