package com.sindaco.matricula.promocion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.matricula.promocion.dto.PromocionRequest;
import com.sindaco.matricula.promocion.entity.Promocion;
import com.sindaco.matricula.promocion.repository.PromocionRepository;

/** Pruebas unitarias del módulo de promociones (CU08). */
@ExtendWith(MockitoExtension.class)
class PromocionServiceImplTest {

    @Mock
    private PromocionRepository repository;

    private PromocionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PromocionServiceImpl(repository);
    }

    private Promocion promocion(boolean vigente, LocalDate inicio, LocalDate fin) {
        return new Promocion("BIENVENIDA", "Descuento de bienvenida",
                new BigDecimal("10.00"), inicio, fin, vigente);
    }

    @Test
    void aplicaDescuentoVigenteAlPrecioDeLaMatricula() {
        when(repository.findByCodigo("BIENVENIDA")).thenReturn(Optional.of(
                promocion(true, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1))));

        BigDecimal resultado = service.aplicarDescuento(new BigDecimal("450.00"), "  bienvenida ");

        assertThat(resultado).isEqualByComparingTo("405.00");
    }

    @Test
    void sinCodigoDevuelveElPrecioOriginal() {
        BigDecimal resultado = service.aplicarDescuento(new BigDecimal("450.00"), "   ");

        assertThat(resultado).isEqualByComparingTo("450.00");
        verify(repository, never()).findByCodigo(any());
    }

    @Test
    void rechazaCodigoDePromocionInexistente() {
        when(repository.findByCodigo("INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.aplicarDescuento(new BigDecimal("450.00"), "INEXISTENTE"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("No existe la promoción");
    }

    @Test
    void rechazaPromocionFueraDeVigencia() {
        when(repository.findByCodigo("ANTIGUA")).thenReturn(Optional.of(
                promocion(true, LocalDate.now().minusDays(30), LocalDate.now().minusDays(1))));

        assertThatThrownBy(() -> service.aplicarDescuento(new BigDecimal("450.00"), "ANTIGUA"))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está vigente");
    }

    @Test
    void rechazaFechaFinAnteriorALaFechaInicio() {
        PromocionRequest request = new PromocionRequest("NUEVA", "Descuento",
                new BigDecimal("10.00"), LocalDate.of(2026, 12, 31),
                LocalDate.of(2026, 12, 1), true);

        assertThatThrownBy(() -> service.crear(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("fecha de fin no puede ser anterior");

        verify(repository, never()).save(any(Promocion.class));
    }
}
