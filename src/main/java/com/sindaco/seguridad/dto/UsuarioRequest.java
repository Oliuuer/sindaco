package com.sindaco.seguridad.dto;

import com.sindaco.seguridad.entity.Rol;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contraseña es obligatoria") @Size(min = 6, message = "Mínimo 6 caracteres") String password,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "El rol es obligatorio") Rol rol,
        @Positive(message = "El id del alumno debe ser positivo") Long alumnoId) {
}
