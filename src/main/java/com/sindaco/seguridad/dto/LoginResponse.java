package com.sindaco.seguridad.dto;

public class LoginResponse {
    private String username;
    private String rol;
    private String token;

    public LoginResponse(String username, String rol, String token) {
        this.username = username;
        this.rol = rol;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getRol() {
        return rol;
    }

    public String getToken() {
        return token;
    }
}
