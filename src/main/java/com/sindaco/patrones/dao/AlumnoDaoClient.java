package com.sindaco.patrones.dao;

/** Cliente del patrón DAO: usa la interfaz sin conocer los detalles de JDBC. */
public class AlumnoDaoClient {

    private final AlumnoDao dao;

    public AlumnoDaoClient(AlumnoDao dao) {
        this.dao = dao;
    }

    public void ejecutar() {
        System.out.println("=== CLIENTE DAO - DATOS REALES DE POSTGRESQL ===");

        System.out.println("\n1) Listado de alumnos:");
        for (AlumnoDto alumno : dao.listar()) {
            System.out.printf("   #%d %s %s - DNI %s%n",
                    alumno.id(), alumno.nombre(), alumno.apellidos(), alumno.dni());
        }

        System.out.println("\n2) Búsqueda por id:");
        dao.buscarPorId(1L).ifPresentOrElse(
                a -> System.out.println("   Encontrado: " + a.nombre() + " " + a.apellidos()),
                () -> System.out.println("   No existe alumno con id 1"));
    }

    public static void main(String[] args) {
        new AlumnoDaoClient(new AlumnoDaoImpl()).ejecutar();
    }
}
