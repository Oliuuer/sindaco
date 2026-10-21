package com.sindaco.instructor.dto;

public class InstructorResponse {
    private Long id;
    private String nombre;
    private String especialidad;
    private String telefono;
    private Boolean disponible;

    public InstructorResponse(Long id, String nombre, String especialidad, String telefono, Boolean disponible) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.telefono = telefono;
        this.disponible = disponible;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public String getTelefono() {
        return telefono;
    }

    public Boolean getDisponible() {
        return disponible;
    }
}
