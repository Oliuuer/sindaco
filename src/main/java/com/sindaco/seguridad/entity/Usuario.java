package com.sindaco.seguridad.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;

    @Column(nullable = false)
    private boolean activo = true;

    /** Alumno vinculado al usuario; permite acotar el historial del rol ALUMNO (CU10). */
    @Column(name = "alumno_id")
    private Long alumnoId;

    public Usuario() {
    }

    public Usuario(String username, String password, String nombre, Rol rol) {
        this(username, password, nombre, rol, null);
    }

    public Usuario(String username, String password, String nombre, Rol rol, Long alumnoId) {
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.rol = rol;
        this.alumnoId = alumnoId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Long getAlumnoId() { return alumnoId; }
    public void setAlumnoId(Long alumnoId) { this.alumnoId = alumnoId; }
}
