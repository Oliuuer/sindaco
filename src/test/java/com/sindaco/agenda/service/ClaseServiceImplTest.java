package com.sindaco.agenda.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;
import com.sindaco.agenda.dto.InstructorResponse;
import com.sindaco.agenda.dto.VehiculoResponse;
import com.sindaco.agenda.entity.Clase;
import com.sindaco.agenda.enums.EstadoClase;
import com.sindaco.agenda.enums.EstadoVehiculo;
import com.sindaco.agenda.repository.ClaseRepository;
import com.sindaco.comun.exception.ConflictoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.matricula.dto.AlumnoResponse;
import com.sindaco.matricula.service.AlumnoService;

/** Pruebas unitarias de disponibilidad de agenda (CU03 y CU09). */
@ExtendWith(MockitoExtension.class)
class ClaseServiceImplTest {

    @Mock
    private ClaseRepository repository;
    @Mock
    private InstructorService instructorService;
    @Mock
    private VehiculoService vehiculoService;
    @Mock
    private AlumnoService alumnoService;

    private ClaseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ClaseServiceImpl(repository, instructorService, vehiculoService, alumnoService);
    }

    private ClaseRequest request() {
        return new ClaseRequest("Clase práctica", "2026-12-01", "10:00", "11:30", 1L, 2L, 3L, null);
    }

    private void datosValidos(EstadoVehiculo estadoVehiculo, boolean instructorDisponible) {
        when(alumnoService.buscarPorId(1L)).thenReturn(
                new AlumnoResponse(1L, "Ana", "Torres", "12345678", "ana@correo.com", "999111222",
                        "Av. Central", LocalDate.now()));
        when(instructorService.buscarPorId(2L)).thenReturn(
                new InstructorResponse(2L, "Carlos López", "Manual", "999555111", instructorDisponible));
        when(vehiculoService.buscarPorId(3L)).thenReturn(
                new VehiculoResponse(3L, "ABC-123", "Toyota", "Corolla", estadoVehiculo));
    }

    @Test
    void rechazaHorarioSolapadoDelInstructorOVehiculo() {
        datosValidos(EstadoVehiculo.OPERATIVO, true);
        when(repository.existeSolapamiento(any(), any(), any(), any(), any(), any())).thenReturn(true);

        assertThatThrownBy(() -> service.crear(request()))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("ya tienen una clase asignada");

        verify(repository, never()).save(any(Clase.class));
    }

    @Test
    void rechazaVehiculoEnTaller() {
        datosValidos(EstadoVehiculo.EN_TALLER, true);

        assertThatThrownBy(() -> service.crear(request()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está operativo");

        verify(repository, never()).save(any(Clase.class));
    }

    @Test
    void rechazaInstructorNoDisponible() {
        datosValidos(EstadoVehiculo.OPERATIVO, false);

        assertThatThrownBy(() -> service.crear(request()))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    void creaClaseProgramadaCuandoNoHayConflicto() {
        datosValidos(EstadoVehiculo.OPERATIVO, true);
        when(repository.existeSolapamiento(any(), any(), any(), any(), any(), any())).thenReturn(false);
        when(repository.save(any(Clase.class))).thenAnswer(invocation -> {
            Clase clase = invocation.getArgument(0);
            clase.setId(5L);
            return clase;
        });

        ClaseResponse respuesta = service.crear(request());

        assertThat(respuesta.estado()).isEqualTo(EstadoClase.PROGRAMADA);
        assertThat(respuesta.fecha()).isEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(respuesta.alumnoNombre()).isEqualTo("Ana Torres");
        verify(repository).save(any(Clase.class));
    }

    @Test
    void validarDisponibilidadNoGuardaCambios() {
        datosValidos(EstadoVehiculo.OPERATIVO, true);
        when(repository.existeSolapamiento(any(), any(), any(), any(), any(), any())).thenReturn(false);

        service.validarDisponibilidad(request(), 9L);

        verify(repository, never()).save(any(Clase.class));
    }
}
