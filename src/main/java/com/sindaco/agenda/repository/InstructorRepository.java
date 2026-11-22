package com.sindaco.agenda.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sindaco.agenda.entity.Instructor;

/** Acceso a datos de instructores (CU04). */
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    /** Instructores que pueden recibir clases. */
    List<Instructor> findByDisponibleTrue();

    /** Instructores filtrados por disponibilidad. */
    List<Instructor> findByDisponible(Boolean disponible);
}
