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
public class PaqueteService {

    private final PaqueteRepository repository;

    public PaqueteService(PaqueteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<PaqueteResponse> listar(boolean soloVigentes) {
        List<Paquete> datos = soloVigentes ? repository.findVigentes() : repository.findAll();
        return datos.stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public PaqueteResponse obtener(Long id) {
        return repository.findById(id)
                .map(this::aRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paquete", id));
    }

    @Transactional
    public PaqueteResponse crear(PaqueteRequest request) {
        Paquete paquete = new Paquete();
        paquete.setNombre(request.nombre());
        paquete.setCategoria(request.categoria());
        paquete.setNumeroClases(request.numeroClases());
        paquete.setPrecio(request.precio());
        paquete.setVigente(true);
        return aRespuesta(repository.save(paquete));
    }

    private PaqueteResponse aRespuesta(Paquete p) {
        return new PaqueteResponse(p.getId(), p.getNombre(), p.getCategoria(),
                p.getNumeroClases(), p.getPrecio(), p.isVigente());
    }
}
