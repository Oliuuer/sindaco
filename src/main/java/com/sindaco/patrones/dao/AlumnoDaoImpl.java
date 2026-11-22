package com.sindaco.patrones.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del DAO. Se conecta directamente a la base de datos
 * PostgreSQL real de SINDACO (tabla 'alumnos').
 */
public class AlumnoDaoImpl implements AlumnoDao {

    private static final String URL = System.getProperty(
            "DB_URL", "jdbc:postgresql://localhost:5432/sindaco");
    private static final String USER = System.getProperty("DB_USER", "postgres");
    private static final String PASSWORD = System.getProperty("DB_PASSWORD", "oli");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("Falta el driver de PostgreSQL en el classpath");
        }
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    @Override
    public List<AlumnoDto> listar() {
        String sql = "SELECT id, nombre, apellidos, dni, email, telefono "
                + "FROM alumnos ORDER BY id";
        List<AlumnoDto> alumnos = new ArrayList<>();

        try (Connection cn = conectar();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                alumnos.add(new AlumnoDto(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("dni"),
                        rs.getString("email"),
                        rs.getString("telefono")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al listar alumnos desde PostgreSQL", e);
        }
        return alumnos;
    }

    @Override
    public Optional<AlumnoDto> buscarPorId(Long id) {
        String sql = "SELECT id, nombre, apellidos, dni, email, telefono "
                + "FROM alumnos WHERE id = ?";

        try (Connection cn = conectar();
                PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new AlumnoDto(
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getString("apellidos"),
                            rs.getString("dni"),
                            rs.getString("email"),
                            rs.getString("telefono")));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Error al buscar el alumno con id " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public int guardar(AlumnoDto alumno) {
        String sql = "INSERT INTO alumnos (nombre, apellidos, dni, email, telefono, fecha_registro) "
                + "VALUES (?, ?, ?, ?, ?, CURRENT_DATE)";

        try (Connection cn = conectar();
                PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, alumno.nombre());
            ps.setString(2, alumno.apellidos());
            ps.setString(3, alumno.dni());
            ps.setString(4, alumno.email());
            ps.setString(5, alumno.telefono());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Error al guardar el alumno", e);
        }
    }
}
