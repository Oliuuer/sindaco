package com.sindaco.seguridad.dto;

public record LoginResponse(String token, String username, String nombre, String rol) {
}
