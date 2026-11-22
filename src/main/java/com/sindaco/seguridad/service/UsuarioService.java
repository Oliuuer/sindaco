package com.sindaco.seguridad.service;

import java.util.List;

import com.sindaco.seguridad.dto.UsuarioRequest;
import com.sindaco.seguridad.dto.UsuarioResponse;

public interface UsuarioService {
    List<UsuarioResponse> listar();
    UsuarioResponse crear(UsuarioRequest request);
    void eliminar(Long id);
}
