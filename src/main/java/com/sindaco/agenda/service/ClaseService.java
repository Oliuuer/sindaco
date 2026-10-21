package com.sindaco.agenda.service;

import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;

import java.util.List;

public interface ClaseService {
    ClaseResponse crear(ClaseRequest request);
    List<ClaseResponse> listar();
}
