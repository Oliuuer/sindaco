package com.sindaco.matricula.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.matricula.dto.PaqueteRequest;
import com.sindaco.matricula.dto.PaqueteResponse;
import com.sindaco.matricula.entity.Paquete;
import com.sindaco.matricula.repository.PaqueteRepository;

@Service
public class PaqueteServiceImpl implements PaqueteService {

    private final PaqueteRepository repository;

    public PaqueteServiceImpl(PaqueteRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaqueteResponse> listar(boolean soloVigentes) {
        List<Paquete> datos = soloVigentes ? repository.findVigentes() : repository.findAll();
        return datos.stream().map(this::aRespuesta).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaqueteResponse obtener(Long id) {
        return repository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete", id));
    }

    @Override
    @Transactional
    public PaqueteResponse crear(PaqueteRequest request) {
        Paquete paquete = new Paquete();
        aplicarRequest(paquete, request);
        paquete.setVigente(true);
        return aRespuesta(repository.save(paquete));
    }

    @Override
    @Transactional
    public PaqueteResponse actualizar(Long id, PaqueteRequest request) {
        Paquete paquete = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete", id));
        aplicarRequest(paquete, request);
        return aRespuesta(repository.save(paquete));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Paquete paquete = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete", id));
        paquete.setVigente(false);
        repository.save(paquete);
    }

    private void aplicarRequest(Paquete paquete, PaqueteRequest request) {
        paquete.setNombre(request.nombre().trim());
        paquete.setCategoria(request.categoria().trim());
        paquete.setNumeroClases(request.numeroClases());
        paquete.setPrecio(request.precio());
    }

    private PaqueteResponse aRespuesta(Paquete p) {
        return new PaqueteResponse(p.getId(), p.getNombre(), p.getCategoria(),
                p.getNumeroClases(), p.getPrecio(), p.isVigente());
    }
}
