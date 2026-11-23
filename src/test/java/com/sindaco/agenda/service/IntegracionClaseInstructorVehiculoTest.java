package com.sindaco.agenda.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.agenda.dto.ClaseRequest;
import com.sindaco.agenda.dto.ClaseResponse;
import com.sindaco.agenda.dto.InstructorRequest;
import com.sindaco.agenda.dto.VehiculoRequest;
import com.sindaco.agenda.enums.EstadoClase;
import com.sindaco.agenda.repository.ClaseRepository;
import com.sindaco.comun.exception.ConflictoException;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.matricula.dto.AlumnoRequest;
import com.sindaco.matricula.service.AlumnoService;

/**
 * Pruebas de integración entre las tablas Clase, Instructor y Vehículo.
 *
 * <p>Contexto Spring real sobre H2 en memoria; PostgreSQL no se ve afectado.</p>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sindaco-integracion-agenda;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class IntegracionClaseInstructorVehiculoTest {

    @Autowired
    private AlumnoService alumnoService;
    @Autowired
    private InstructorService instructorService;
    @Autowired
    private VehiculoService vehiculoService;
    @Autowired
    private ClaseService claseService;
    @Autowired
    private ClaseRepository claseRepository;

    private Long crearAlumno(String dni) {
        return alumnoService.crear(new AlumnoRequest("Ana", "Torres", dni, "ana@correo.com",
                "999111222", "Av. Central", "2026-01-10")).id();
    }

    private Long crearInstructor(String nombre) {
        return instructorService.crear(new InstructorRequest(nombre, "Manual", "999555111", true)).id();
    }

    private Long crearVehiculo(String placa, String estado) {
        return vehiculoService.crear(new VehiculoRequest(placa, "Toyota", "Corolla", estado)).id();
    }

    private ClaseRequest clase(Long alumnoId, Long instructorId, Long vehiculoId,
                               String inicio, String fin) {
        return new ClaseRequest("Clase práctica", "2026-12-01", inicio, fin,
                alumnoId, instructorId, vehiculoId, null);
    }

    @Test
    void claseQuedaProgramadaConAlumnoInstructorYVehiculoReales() {
        long antes = claseRepository.count();
        Long alumnoId = crearAlumno("11111112");
        Long instructorId = crearInstructor("Carlos López");
        Long vehiculoId = crearVehiculo("ABC-124", "OPERATIVO");

        ClaseResponse clase = claseService.crear(clase(alumnoId, instructorId, vehiculoId, "10:00", "11:30"));

        assertThat(clase.estado()).isEqualTo(EstadoClase.PROGRAMADA);
        assertThat(clase.fecha()).isEqualTo(java.time.LocalDate.of(2026, 12, 1));
        assertThat(clase.alumnoNombre()).isEqualTo("Ana Torres");
        assertThat(clase.instructorNombre()).isEqualTo("Carlos López");
        assertThat(clase.vehiculoPlaca()).isEqualTo("ABC-124");
        assertThat(claseRepository.count()).isEqualTo(antes + 1);
    }

    @Test
    void rechazaSegundaClaseSolapadaConElMismoInstructorYVehiculo() {
        Long alumnoId = crearAlumno("22222223");
        Long instructorId = crearInstructor("María Pérez");
        Long vehiculoId = crearVehiculo("ABC-125", "OPERATIVO");
        claseService.crear(clase(alumnoId, instructorId, vehiculoId, "10:00", "11:30"));
        long despuesDeLaPrimera = claseRepository.count();

        assertThatThrownBy(() -> claseService.crear(
                clase(alumnoId, instructorId, vehiculoId, "10:30", "11:00")))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("ya tienen una clase asignada");

        assertThat(claseRepository.count()).isEqualTo(despuesDeLaPrimera);
    }

    @Test
    void rechazaClaseConVehiculoEnTaller() {
        Long alumnoId = crearAlumno("33333334");
        Long instructorId = crearInstructor("Jorge Ruiz");
        Long vehiculoId = crearVehiculo("ABC-126", "EN_TALLER");
        long antes = claseRepository.count();

        assertThatThrownBy(() -> claseService.crear(
                clase(alumnoId, instructorId, vehiculoId, "10:00", "11:30")))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("no está operativo");

        assertThat(claseRepository.count()).isEqualTo(antes);
    }

    @Test
    void eliminarClaseNoAfectaAlumnoInstructorNiVehiculo() {
        Long alumnoId = crearAlumno("44444445");
        Long instructorId = crearInstructor("Lucía Soto");
        Long vehiculoId = crearVehiculo("ABC-127", "OPERATIVO");
        ClaseResponse clase = claseService.crear(clase(alumnoId, instructorId, vehiculoId, "09:00", "10:00"));

        assertThat(claseService.listarPorAlumno(alumnoId)).hasSize(1);
        assertThat(claseService.listarPorInstructor(instructorId)).hasSize(1);

        claseService.eliminar(clase.id());

        assertThat(claseService.listarPorAlumno(alumnoId)).isEmpty();
        assertThat(claseService.listarPorInstructor(instructorId)).isEmpty();
        assertThat(instructorService.buscarPorId(instructorId).nombre()).isEqualTo("Lucía Soto");
        assertThat(vehiculoService.buscarPorId(vehiculoId).placa()).isEqualTo("ABC-127");
    }
}
