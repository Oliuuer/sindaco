package com.sindaco.matricula.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.comun.notificacion.service.NotificacionService;
import com.sindaco.matricula.dto.PagoRequest;
import com.sindaco.matricula.dto.PagoResponse;
import com.sindaco.matricula.entity.Matricula;
import com.sindaco.matricula.entity.Pago;
import com.sindaco.matricula.enums.EstadoMatricula;
import com.sindaco.matricula.enums.TipoPago;
import com.sindaco.matricula.repository.AlumnoRepository;
import com.sindaco.matricula.repository.MatriculaRepository;
import com.sindaco.matricula.repository.PagoRepository;
import com.sindaco.matricula.repository.PaqueteRepository;

/** Pruebas unitarias de las reglas de saldo y pago fraccionado (CU02). */
@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock
    private PagoRepository repository;
    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private AlumnoRepository alumnoRepository;
    @Mock
    private PaqueteRepository paqueteRepository;
    @Mock
    private NotificacionService notificacionService;

    private PagoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PagoServiceImpl(repository, matriculaRepository, alumnoRepository,
                paqueteRepository, notificacionService);
    }

    private Matricula matricula(EstadoMatricula estado) {
        Matricula matricula = new Matricula(1L, 10L, estado, LocalDate.now());
        matricula.setId(100L);
        matricula.setPrecioFinal(new BigDecimal("450.00"));
        return matricula;
    }

    private PagoRequest pago(String monto) {
        return new PagoRequest(100L, new BigDecimal(monto), "YAPE", null, null);
    }

    @Test
    void pagoParcialDejaSaldoYMatriculaPendiente() {
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula(EstadoMatricula.POR_PAGAR)));
        when(repository.sumarPorMatricula(100L))
                .thenReturn(BigDecimal.ZERO, new BigDecimal("300.00"), new BigDecimal("300.00"));
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            pago.setId(1L);
            return pago;
        });

        PagoResponse respuesta = service.crear(pago("300.00"));

        assertThat(respuesta.saldoPendiente()).isEqualByComparingTo("150.00");
        assertThat(respuesta.tipo()).isEqualTo("FRACCIONADO");
        assertThat(matriculaRepository.findById(100L).orElseThrow().getEstado())
                .isEqualTo(EstadoMatricula.POR_PAGAR);

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(repository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getTipoPago()).isEqualTo(TipoPago.FRACCIONADO);
        verify(notificacionService).registrar(eq("PAGO"), anyString(), anyString(),
                eq("ADMINISTRADOR"), eq(1L));
    }

    @Test
    void pagoTotalConfirmaLaMatricula() {
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula(EstadoMatricula.POR_PAGAR)));
        when(repository.sumarPorMatricula(100L))
                .thenReturn(BigDecimal.ZERO, new BigDecimal("450.00"), new BigDecimal("450.00"));
        when(repository.save(any(Pago.class))).thenAnswer(invocation -> {
            Pago pago = invocation.getArgument(0);
            pago.setId(2L);
            return pago;
        });

        PagoResponse respuesta = service.crear(pago("450.00"));

        assertThat(respuesta.saldoPendiente()).isEqualByComparingTo("0.00");
        assertThat(respuesta.tipo()).isEqualTo("TOTAL");
        assertThat(matriculaRepository.findById(100L).orElseThrow().getEstado())
                .isEqualTo(EstadoMatricula.CONFIRMADA);
    }

    @Test
    void rechazaPagoMayorAlSaldoPendiente() {
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula(EstadoMatricula.POR_PAGAR)));
        when(repository.sumarPorMatricula(100L)).thenReturn(new BigDecimal("400.00"));

        assertThatThrownBy(() -> service.crear(pago("200.00")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("supera el saldo pendiente");

        verify(repository, never()).save(any(Pago.class));
        verify(notificacionService, never()).registrar(anyString(), anyString(), anyString(),
                anyString(), any());
    }

    @Test
    void eliminarPagoRecalculaElEstadoDeLaMatricula() {
        Matricula matricula = matricula(EstadoMatricula.CONFIRMADA);
        Pago pago = new Pago(100L, new BigDecimal("450.00"), "YAPE", "B-000001", LocalDate.now());
        pago.setId(3L);

        when(repository.findById(3L)).thenReturn(Optional.of(pago));
        when(matriculaRepository.findById(100L)).thenReturn(Optional.of(matricula));
        when(repository.sumarPorMatricula(100L)).thenReturn(new BigDecimal("200.00"));

        service.eliminar(3L);

        assertThat(matricula.getEstado()).isEqualTo(EstadoMatricula.POR_PAGAR);
        verify(matriculaRepository).save(matricula);
    }
}
