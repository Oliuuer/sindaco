package com.sindaco.matricula.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.comun.exception.RecursoNoEncontradoException;
import com.sindaco.matricula.dto.AlumnoRequest;
import com.sindaco.matricula.dto.MatriculaRequest;
import com.sindaco.matricula.dto.PagoRequest;
import com.sindaco.matricula.dto.PagoResponse;
import com.sindaco.matricula.entity.Paquete;
import com.sindaco.matricula.enums.EstadoMatricula;
import com.sindaco.matricula.enums.TipoPago;
import com.sindaco.matricula.repository.MatriculaRepository;
import com.sindaco.matricula.repository.PagoRepository;
import com.sindaco.matricula.repository.PaqueteRepository;

/**
 * Pruebas de integración entre las tablas Alumno, Matrícula y Pago.
 *
 * <p>Levanta el contexto real de Spring con una base de datos H2 en memoria,
 * de modo que se ejercitan servicios, repositorios y consultas JPQL reales.
 * PostgreSQL no se ve afectado.</p>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sindaco-integracion;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class IntegracionAlumnoMatriculaPagoTest {

    @Autowired
    private AlumnoService alumnoService;
    @Autowired
    private MatriculaService matriculaService;
    @Autowired
    private PagoService pagoService;
    @Autowired
    private MatriculaRepository matriculaRepository;
    @Autowired
    private PagoRepository pagoRepository;
    @Autowired
    private PaqueteRepository paqueteRepository;

    private Long crearAlumno(String dni) {
        return alumnoService.crear(new AlumnoRequest("Ana", "Torres", dni, "ana@correo.com",
                "999111222", "Av. Central", "2026-01-10")).id();
    }

    private Long crearPaquete(String precio) {
        Paquete paquete = new Paquete();
        paquete.setNombre("Paquete 20 clases");
        paquete.setCategoria("LICENCIA");
        paquete.setNumeroClases(20);
        paquete.setPrecio(new BigDecimal(precio));
        paquete.setVigente(true);
        return paqueteRepository.save(paquete).getId();
    }

    @Test
    void alumnoMatriculaYPagoParcialSeReflejanEnLasTresTablas() {
        Long alumnoId = crearAlumno("11111111");
        Long paqueteId = crearPaquete("450.00");
        Long matriculaId = matriculaService.crear(new MatriculaRequest(alumnoId, paqueteId,
                null, "2026-01-10", null)).id();

        PagoResponse abono = pagoService.crear(new PagoRequest(matriculaId,
                new BigDecimal("300.00"), "YAPE", null, "2026-01-11"));

        assertThat(abono.saldoPendiente()).isEqualByComparingTo("150.00");
        assertThat(abono.tipo()).isEqualTo(TipoPago.FRACCIONADO.name());
        assertThat(abono.comprobante()).startsWith("B-");
        assertThat(matriculaService.buscarPorId(matriculaId).estado())
                .isEqualTo(EstadoMatricula.POR_PAGAR);
        assertThat(pagoService.listarPorAlumno(alumnoId)).hasSize(1);
    }

    @Test
    void pagoTotalConfirmaLaMatriculaYElComprobanteSePuedeConsultar() {
        Long alumnoId = crearAlumno("22222222");
        Long paqueteId = crearPaquete("450.00");
        Long matriculaId = matriculaService.crear(new MatriculaRequest(alumnoId, paqueteId,
                null, "2026-01-10", null)).id();

        PagoResponse pago = pagoService.crear(new PagoRequest(matriculaId,
                new BigDecimal("450.00"), "EFECTIVO", null, "2026-01-12"));

        assertThat(pago.saldoPendiente()).isEqualByComparingTo("0.00");
        assertThat(pago.tipo()).isEqualTo(TipoPago.TOTAL.name());
        assertThat(matriculaService.buscarPorId(matriculaId).estado())
                .isEqualTo(EstadoMatricula.CONFIRMADA);
        assertThat(pagoService.comprobante(pago.id()).alumno()).isEqualTo("Ana Torres");
        assertThat(pagoService.comprobante(pago.id()).paquete()).isEqualTo("Paquete 20 clases");
    }

    @Test
    void eliminarPagoDevuelveLaMatriculaAPorPagar() {
        Long alumnoId = crearAlumno("33333333");
        Long paqueteId = crearPaquete("450.00");
        Long matriculaId = matriculaService.crear(new MatriculaRequest(alumnoId, paqueteId,
                null, "2026-01-10", null)).id();
        PagoResponse pago = pagoService.crear(new PagoRequest(matriculaId,
                new BigDecimal("450.00"), "PLIN", null, "2026-01-12"));

        pagoService.eliminar(pago.id());

        assertThat(matriculaService.buscarPorId(matriculaId).estado())
                .isEqualTo(EstadoMatricula.POR_PAGAR);
        assertThat(pagoService.listarPorMatricula(matriculaId)).isEmpty();
    }

    @Test
    void noSePuedeMatricularUnAlumnoInexistente() {
        long matriculasAntes = matriculaRepository.count();
        long pagosAntes = pagoRepository.count();
        Long paqueteId = crearPaquete("450.00");

        assertThatThrownBy(() -> matriculaService.crear(new MatriculaRequest(999L, paqueteId,
                null, "2026-01-10", null)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Alumno");

        assertThat(matriculaRepository.count()).isEqualTo(matriculasAntes);
        assertThat(pagoRepository.count()).isEqualTo(pagosAntes);
    }

    @Test
    void losPagosDelPortalSeFiltranPorElAlumnoCorrecto() {
        Long alumnoUno = crearAlumno("44444444");
        Long alumnoDos = crearAlumno("55555555");
        Long paqueteId = crearPaquete("450.00");
        Long matriculaUno = matriculaService.crear(new MatriculaRequest(alumnoUno, paqueteId,
                null, "2026-01-10", null)).id();
        Long matriculaDos = matriculaService.crear(new MatriculaRequest(alumnoDos, paqueteId,
                null, "2026-01-10", null)).id();
        pagoService.crear(new PagoRequest(matriculaUno, new BigDecimal("100.00"), "YAPE", null, "2026-01-11"));
        pagoService.crear(new PagoRequest(matriculaDos, new BigDecimal("200.00"), "YAPE", null, "2026-01-11"));

        List<PagoResponse> pagosDelAlumnoUno = pagoService.listarPorAlumno(alumnoUno);

        assertThat(pagosDelAlumnoUno).hasSize(1);
        assertThat(pagosDelAlumnoUno.get(0).monto()).isEqualByComparingTo("100.00");
        assertThat(pagosDelAlumnoUno.get(0).fecha()).isEqualTo(LocalDate.of(2026, 1, 11));
        assertThat(pagoService.listarPorAlumno(alumnoDos)).hasSize(1);
    }
}
