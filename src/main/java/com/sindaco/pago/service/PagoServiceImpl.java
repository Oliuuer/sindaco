package com.sindaco.pago.service;

import com.sindaco.pago.dto.PagoRequest;
import com.sindaco.pago.dto.PagoResponse;
import com.sindaco.pago.entity.Pago;
import com.sindaco.pago.repository.PagoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository repository;

    public PagoServiceImpl(PagoRepository repository) {
        this.repository = repository;
    }

    @Override
    public PagoResponse crear(PagoRequest request) {
        if (request.getMatriculaId() == null) {
            throw new IllegalArgumentException("La matrícula es obligatoria");
        }

        if (request.getMonto() == null) {
            throw new IllegalArgumentException("El monto es obligatorio");
        }

        Pago pago = new Pago(
            request.getMatriculaId(),
            request.getMonto(),
            request.getConcepto() != null ? request.getConcepto() : "PAGO",
            request.getFecha() != null ? LocalDate.parse(request.getFecha()) : LocalDate.now()
        );

        Pago guardado = repository.save(pago);

        return new PagoResponse(
            guardado.getId(),
            guardado.getMatriculaId(),
            guardado.getMonto(),
            guardado.getConcepto(),
            guardado.getFecha()
        );
    }

    @Override
    public List<PagoResponse> listar() {
        return repository.findAll().stream()
            .map(p -> new PagoResponse(
                p.getId(),
                p.getMatriculaId(),
                p.getMonto(),
                p.getConcepto(),
                p.getFecha()
            ))
            .collect(Collectors.toList());
    }
}
