package com.sindaco.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.academico.entity.AsistenciaEvaluacion;
import com.sindaco.academico.entity.Reprogramacion;
import com.sindaco.academico.enums.EstadoReprogramacion;
import com.sindaco.academico.repository.AsistenciaRepository;
import com.sindaco.academico.repository.ReprogramacionRepository;
import com.sindaco.agenda.entity.Clase;
import com.sindaco.agenda.entity.Instructor;
import com.sindaco.agenda.entity.Vehiculo;
import com.sindaco.agenda.enums.EstadoClase;
import com.sindaco.agenda.enums.EstadoVehiculo;
import com.sindaco.agenda.repository.ClaseRepository;
import com.sindaco.agenda.repository.InstructorRepository;
import com.sindaco.agenda.repository.VehiculoRepository;
import com.sindaco.mantenimiento.entity.Mantenimiento;
import com.sindaco.mantenimiento.enums.EstadoMantenimiento;
import com.sindaco.mantenimiento.enums.TipoMantenimiento;
import com.sindaco.mantenimiento.repository.MantenimientoRepository;
import com.sindaco.matricula.entity.Alumno;
import com.sindaco.matricula.entity.Matricula;
import com.sindaco.matricula.entity.Pago;
import com.sindaco.matricula.entity.Paquete;
import com.sindaco.matricula.enums.EstadoMatricula;
import com.sindaco.matricula.enums.TipoPago;
import com.sindaco.matricula.promocion.entity.Promocion;
import com.sindaco.matricula.promocion.repository.PromocionRepository;
import com.sindaco.matricula.repository.AlumnoRepository;
import com.sindaco.matricula.repository.MatriculaRepository;
import com.sindaco.matricula.repository.PagoRepository;
import com.sindaco.matricula.repository.PaqueteRepository;
import com.sindaco.seguridad.entity.Rol;
import com.sindaco.seguridad.entity.Usuario;
import com.sindaco.seguridad.repository.UsuarioRepository;

/** Capa de infraestructura: precarga usuarios y datos de ejemplo una sola vez. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlumnoRepository alumnoRepository;
    private final PaqueteRepository paqueteRepository;
    private final MatriculaRepository matriculaRepository;
    private final PagoRepository pagoRepository;
    private final PromocionRepository promocionRepository;
    private final InstructorRepository instructorRepository;
    private final VehiculoRepository vehiculoRepository;
    private final ClaseRepository claseRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final ReprogramacionRepository reprogramacionRepository;
    private final MantenimientoRepository mantenimientoRepository;

    public DataSeeder(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                      AlumnoRepository alumnoRepository, PaqueteRepository paqueteRepository,
                      MatriculaRepository matriculaRepository, PagoRepository pagoRepository,
                      PromocionRepository promocionRepository, InstructorRepository instructorRepository, VehiculoRepository vehiculoRepository,
                      ClaseRepository claseRepository, AsistenciaRepository asistenciaRepository,
                      ReprogramacionRepository reprogramacionRepository,
                      MantenimientoRepository mantenimientoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.alumnoRepository = alumnoRepository;
        this.paqueteRepository = paqueteRepository;
        this.matriculaRepository = matriculaRepository;
        this.pagoRepository = pagoRepository;
        this.promocionRepository = promocionRepository;
        this.instructorRepository = instructorRepository;
        this.vehiculoRepository = vehiculoRepository;
        this.claseRepository = claseRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.reprogramacionRepository = reprogramacionRepository;
        this.mantenimientoRepository = mantenimientoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (alumnoRepository.count() == 0) {
            sembrarDatosAcademicos();
        }
        if (usuarioRepository.count() == 0) {
            sembrarUsuarios();
        }
        if (promocionRepository.count() == 0) {
            sembrarPromociones();
        }
        normalizarTiposPago();
        vincularAlumnoUsuario();
    }

    /** Completa el tipo de los pagos creados antes de añadir la columna tipo_pago. */
    private void normalizarTiposPago() {
        pagoRepository.findAll().stream()
                .filter(pago -> pago.getTipoPago() == null)
                .forEach(pago -> {
                    pago.setTipoPago(TipoPago.TOTAL);
                    pagoRepository.save(pago);
                });
    }

    private void sembrarPromociones() {
        LocalDate hoy = LocalDate.now();
        promocionRepository.save(new Promocion("PROMO2026", "Descuento de temporada para matrículas 2026",
                new BigDecimal("15.00"), hoy.minusDays(30), hoy.plusDays(60), true));
        promocionRepository.save(new Promocion("PROMOANTIGUA", "Promoción de campaña anterior",
                new BigDecimal("10.00"), hoy.minusDays(120), hoy.minusDays(60), false));
    }

    private void sembrarUsuarios() {
        Long alumnoId = alumnoRepository.findAll().stream().findFirst().map(Alumno::getId).orElse(null);
        usuarioRepository.save(new Usuario("propietario@sindaco.com", passwordEncoder.encode("pass123"),
                "Juan Pérez", Rol.PROPIETARIO));
        usuarioRepository.save(new Usuario("admin@sindaco.com", passwordEncoder.encode("pass123"),
                "María García", Rol.ADMINISTRADOR));
        usuarioRepository.save(new Usuario("recepcionista@sindaco.com", passwordEncoder.encode("pass123"),
                "Ana Ramos", Rol.RECEPCIONISTA));
        usuarioRepository.save(new Usuario("encargado@sindaco.com", passwordEncoder.encode("pass123"),
                "Luis Vega", Rol.ENCARGADO_FLOTAS));
        usuarioRepository.save(new Usuario("instructor@sindaco.com", passwordEncoder.encode("pass123"),
                "Carlos López", Rol.INSTRUCTOR));
        usuarioRepository.save(new Usuario("alumno@sindaco.com", passwordEncoder.encode("pass123"),
                "Pedro Martínez", Rol.ALUMNO, alumnoId));
    }

    /** Garantiza que el usuario alumno tenga vinculado un alumno aunque ya existiera en la BD. */
    private void vincularAlumnoUsuario() {
        usuarioRepository.findByUsername("alumno@sindaco.com").ifPresent(usuario -> {
            if (usuario.getAlumnoId() == null) {
                alumnoRepository.findAll().stream().findFirst().map(Alumno::getId)
                        .ifPresent(alumnoId -> {
                            usuario.setAlumnoId(alumnoId);
                            usuarioRepository.save(usuario);
                        });
            }
        });
    }

    private void sembrarDatosAcademicos() {
        Paquete p1 = paquete(new Paquete(), "A-I", "A-I", 10, "450.00", true);
        Paquete p2 = paquete(new Paquete(), "A-IIa", "A-IIa", 15, "620.00", true);
        Paquete p3 = paquete(new Paquete(), "A-IIb", "A-IIb", 18, "680.00", true);
        Paquete p4 = paquete(new Paquete(), "A-IIIb", "A-IIIb", 20, "760.00", true);
        paqueteRepository.save(p1);
        paqueteRepository.save(p2);
        paqueteRepository.save(p3);
        paqueteRepository.save(p4);

        Alumno a1 = alumno(new Alumno(), "Ana", "Torres", "12345678", "ana@correo.com", "999111222",
                "Av. Central 123");
        Alumno a2 = alumno(new Alumno(), "Luis", "Ramírez", "98765432", "luis@correo.com", "998877665",
                "Jr. Las Flores 45");
        Alumno a3 = alumno(new Alumno(), "Carlos", "Vega", "45678912", "carlos@correo.com", "955441233",
                "Calle Lima 88");
        alumnoRepository.save(a1);
        alumnoRepository.save(a2);
        alumnoRepository.save(a3);

        LocalDate hoy = LocalDate.now();
        Matricula m1 = matricula(a1.getId(), p2.getId(), EstadoMatricula.CONFIRMADA, hoy.minusDays(10));
        Matricula m2 = matricula(a2.getId(), p3.getId(), EstadoMatricula.POR_PAGAR, hoy.minusDays(8));
        Matricula m3 = matricula(a3.getId(), p1.getId(), EstadoMatricula.CONFIRMADA, hoy.minusDays(5));
        matriculaRepository.save(m1);
        matriculaRepository.save(m2);
        matriculaRepository.save(m3);

        pagoRepository.save(new Pago(m1.getId(), new BigDecimal("620.00"), "YAPE", "Y-001", hoy.minusDays(10)));
        pagoRepository.save(new Pago(m2.getId(), new BigDecimal("680.00"), "TRANSFERENCIA", "TR-102", hoy.minusDays(8)));

        Instructor i1 = instructorRepository.save(new Instructor("Carlos López", "Manual", "999555111", true));
        Instructor i2 = instructorRepository.save(new Instructor("Renzo Castro", "Automático", "980123456", true));

        Vehiculo v1 = vehiculoRepository.save(new Vehiculo("ABC-123", "Toyota", "Corolla", EstadoVehiculo.OPERATIVO));
        Vehiculo v2 = vehiculoRepository.save(new Vehiculo("XYZ-456", "Honda", "Civic", EstadoVehiculo.EN_TALLER));
        Vehiculo v3 = vehiculoRepository.save(new Vehiculo("QWE-789", "Hyundai", "Accent", EstadoVehiculo.OPERATIVO));

        Clase c1 = clase("Clase práctica 01", a1.getId(), i1.getId(), v1.getId(),
                hoy.plusDays(2), LocalTime.of(8, 0), LocalTime.of(9, 30), EstadoClase.PROGRAMADA);
        Clase c2 = clase("Clase práctica 02", a2.getId(), i2.getId(), v3.getId(),
                hoy.plusDays(2), LocalTime.of(10, 0), LocalTime.of(11, 30), EstadoClase.CONFIRMADA);
        Clase c3 = clase("Clase práctica 03", a3.getId(), i1.getId(), v3.getId(),
                hoy.plusDays(3), LocalTime.of(15, 0), LocalTime.of(16, 30), EstadoClase.PROGRAMADA);
        claseRepository.save(c1);
        claseRepository.save(c2);
        claseRepository.save(c3);

        asistenciaRepository.save(asistencia(c1.getId(), a1.getId(), true, 17,
                "Muy buen desempeño", hoy.minusDays(1)));
        asistenciaRepository.save(asistencia(c2.getId(), a2.getId(), false, 0,
                "Falta justificada", hoy.minusDays(1)));

        reprogramacionRepository.save(reprogramacion(a2.getId(), c2.getId(), "Emergencia familiar",
                hoy.minusDays(1), EstadoReprogramacion.PENDIENTE_REPROGRAMACION));
        reprogramacionRepository.save(reprogramacion(a1.getId(), c1.getId(), "No pudo asistir",
                hoy.minusDays(2), EstadoReprogramacion.REPROGRAMADA));

        mantenimientoRepository.save(mantenimiento(v2.getId(), TipoMantenimiento.PREVENTIVO,
                "Cambio de aceite y revisión general", hoy.minusDays(3),
                EstadoMantenimiento.EN_PROCESO, "Taller AutoMax", new BigDecimal("180.00")));
        mantenimientoRepository.save(mantenimiento(v1.getId(), TipoMantenimiento.CORRECTIVO,
                "Ajuste de frenos", hoy.minusDays(1),
                EstadoMantenimiento.COMPLETADO, "Mecánica Central", new BigDecimal("120.00")));
    }

    private Paquete paquete(Paquete p, String nombre, String categoria, int clases, String precio, boolean vigente) {
        p.setNombre(nombre);
        p.setCategoria(categoria);
        p.setNumeroClases(clases);
        p.setPrecio(new BigDecimal(precio));
        p.setVigente(vigente);
        return p;
    }

    private Alumno alumno(Alumno a, String nombre, String apellidos, String dni, String email,
                          String telefono, String direccion) {
        a.setNombre(nombre);
        a.setApellidos(apellidos);
        a.setDni(dni);
        a.setEmail(email);
        a.setTelefono(telefono);
        a.setDireccion(direccion);
        a.setFechaRegistro(LocalDate.now().minusDays(15));
        return a;
    }

    private Matricula matricula(Long alumnoId, Long paqueteId, EstadoMatricula estado, LocalDate fecha) {
        Matricula m = new Matricula();
        m.setAlumnoId(alumnoId);
        m.setPaqueteId(paqueteId);
        m.setEstado(estado);
        m.setFecha(fecha);
        return m;
    }

    private Clase clase(String nombre, Long alumnoId, Long instructorId, Long vehiculoId,
                        LocalDate fecha, LocalTime inicio, LocalTime fin, EstadoClase estado) {
        return new Clase(nombre, alumnoId, instructorId, vehiculoId, fecha, inicio, fin, estado);
    }

    private AsistenciaEvaluacion asistencia(Long claseId, Long alumnoId, boolean asistio, Integer calificacion,
                                             String observaciones, LocalDate fecha) {
        AsistenciaEvaluacion a = new AsistenciaEvaluacion();
        a.setClaseId(claseId);
        a.setAlumnoId(alumnoId);
        a.setAsistio(asistio);
        a.setCalificacion(calificacion);
        a.setObservaciones(observaciones);
        a.setFecha(fecha);
        return a;
    }

    private Reprogramacion reprogramacion(Long alumnoId, Long claseId, String motivo,
                                           LocalDate fecha, EstadoReprogramacion estado) {
        Reprogramacion r = new Reprogramacion();
        r.setAlumnoId(alumnoId);
        r.setClaseId(claseId);
        r.setMotivo(motivo);
        r.setFecha(fecha);
        r.setEstado(estado);
        return r;
    }

    private Mantenimiento mantenimiento(Long vehiculoId, TipoMantenimiento tipo, String descripcion,
                                         LocalDate fecha, EstadoMantenimiento estado, String proveedor,
                                         BigDecimal costo) {
        Mantenimiento m = new Mantenimiento();
        m.setVehiculoId(vehiculoId);
        m.setTipo(tipo);
        m.setDescripcion(descripcion);
        m.setFecha(fecha);
        m.setEstado(estado);
        m.setProveedor(proveedor);
        m.setCosto(costo);
        return m;
    }
}
