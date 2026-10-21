# SINDACO – Sistema web de gestión de la Escuela de Manejo

Proyecto del curso **Análisis y Diseño de Sistemas de Información (UTP)**. Sistema web para gestionar matrículas, pagos, clases, instructores, vehículos, asistencia y mantenimiento de una escuela de manejo.

> Estado actual: **arquitectura base lista**. El frontend ya existe (con datos de prueba) y el backend tiene la estructura por módulos, la configuración y **un ejemplo completo** (Paquetes, CU08) que sirve de molde para los demás casos de uso.

## 1. Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.x (Spring MVC, API REST) |
| Persistencia | Spring Data JPA + Hibernate (ORM), consultas JPQL |
| Base de datos | PostgreSQL |
| Seguridad | Spring Security (JWT en una fase posterior) |
| Validación | Jakarta Bean Validation |
| Construcción | Maven |
| Frontend | HTML + CSS + JavaScript (carpeta `src/main/resources/static`) |

## 2. Requisitos previos

- JDK 21
- Maven 3.9+ (o el Maven integrado de IntelliJ IDEA / Eclipse)
- PostgreSQL 14 o superior

## 3. Cómo ejecutar

1. Crear la base de datos:
   ```sql
   CREATE DATABASE sindaco;
   ```
2. Configurar las credenciales. Por defecto usa `postgres / postgres` en `localhost:5432`. Para cambiarlas, define las variables `DB_URL`, `DB_USER` y `DB_PASSWORD` (ver `.env.example`) o edita `src/main/resources/application.properties`.
3. Compilar y ejecutar:
   ```bash
   mvn clean package
   mvn spring-boot:run
   ```
4. Abrir `http://localhost:8080/` para ver el frontend.
5. Probar el ejemplo de la API:
   - `GET  http://localhost:8080/api/paquetes`
   - `POST http://localhost:8080/api/paquetes` con el cuerpo
     `{"nombre":"A-I","categoria":"A-I","numeroClases":10,"precio":450.00}`

## 4. Arquitectura

Arquitectura en capas con el patrón **MVC**. En una API REST la *Vista* es el frontend; el backend contiene Controlador, Modelo (servicio, repositorio y entidad) y DTO.

```
Frontend (index.html + app.js)
        │  fetch  /api/...   (JSON)
        ▼
Controller   → recibe la petición y valida el formato (@Valid). Sin lógica de negocio.
        ▼
Service      → lógica de cada caso de uso, reglas y transacciones (@Transactional).
        ▼
Repository   → acceso a datos con Spring Data JPA y consultas JPQL.
        ▼
Entity       → clases mapeadas por Hibernate a tablas de PostgreSQL.
```

Reglas de la arquitectura:

1. El **controlador no tiene lógica**: solo llama al servicio.
2. Las **entidades nunca salen al frontend**: se convierten a DTO (`...Request` entrada, `...Response` salida).
3. Cada **consulta JPQL** vive en el repositorio de su entidad.
4. Los **errores** se devuelven siempre con el formato `ApiError` (`comun/exception`).
5. Un módulo **no accede directo a los repositorios de otro**: usa su servicio.

## 5. Estructura de carpetas

Organizada **por módulo** (los mismos 4 del diagrama general de casos de uso), cada uno con sus capas.

```
sindaco/
├── pom.xml
├── README.md
├── .env.example
├── .gitignore
├── docs/                          Documentación y diagramas del informe
└── src/
    ├── main/
    │   ├── java/com/sindaco/
    │   │   ├── SindacoApplication.java     Punto de entrada
    │   │   ├── config/                     SecurityConfig (JWT y roles después)
    │   │   ├── comun/                      Código compartido
    │   │   │   └── exception/              ApiError, GlobalExceptionHandler
    │   │   ├── seguridad/                  Usuarios, roles, login
    │   │   ├── matricula/                  Matrícula y Pagos (CU01, CU02, CU08)
    │   │   │   ├── controller/  service/  repository/  entity/  dto/
    │   │   ├── agenda/                     Agenda y Flota (CU03, CU04)
    │   │   ├── academico/                  Académico y Reportes (CU05, CU06, CU09, CU10)
    │   │   └── mantenimiento/              Mantenimiento (CU07)
    │   └── resources/
    │       ├── application.properties      Configuración y conexión a PostgreSQL
    │       └── static/                     Frontend (index.html, app.js, estilo.css)
    └── test/java/com/sindaco/              Pruebas
```

## 6. Módulos y casos de uso

| Módulo | Caso de uso | Entidades | Endpoints previstos | Estado |
|---|---|---|---|---|
| matricula | CU01 Registrar Matrícula | Alumno, Matricula | `/api/alumnos`, `/api/matriculas` | Pendiente |
| matricula | CU02 Registrar Pago y Emisión de Comprobante | Pago, Comprobante | `/api/pagos` | Pendiente |
| matricula | CU08 Gestionar Paquetes y Promociones | Paquete | `/api/paquetes` | **Ejemplo hecho** |
| agenda | CU03 Programar Clases | Clase | `/api/clases` | Pendiente |
| agenda | CU04 Asignar Instructor y Vehículo | Instructor, Vehiculo | `/api/instructores`, `/api/vehiculos` | Pendiente |
| academico | CU05 Registrar Asistencia y Evaluación | AsistenciaEvaluacion | `/api/asistencias` | Pendiente |
| academico | CU09 Controlar Ausencias y Reprogramaciones | Reprogramacion | `/api/reprogramaciones` | Pendiente |
| academico | CU10 Consultar Historial Académico y Financiero | (consulta) | `/api/historial/alumnos/{id}` | Pendiente |
| academico | CU06 Consolidar y Generar Reporte de Ingresos | (consulta) | `/api/reportes/ingresos` | Pendiente |
| mantenimiento | CU07 Gestionar Mantenimiento de Vehículos | Mantenimiento | `/api/mantenimientos` | Pendiente |
| seguridad | Inicio de sesión y roles | Usuario, Rol | `/api/auth/login` | Pendiente |

## 7. El frontend actual frente al sistema

El frontend es una aplicación de una página con datos simulados en `localStorage`. Tiene 8 secciones y 4 roles.

| Sección del frontend | Casos de uso que cubre | Datos que maneja |
|---|---|---|
| Dashboard | Resumen general | todos |
| Alumnos | CU01 | nombre, apellidos, dni, email, teléfono, dirección, fecha de registro |
| Matrículas | CU01 | alumno, paquete, fecha, estado |
| Pagos | CU02 | matrícula, monto, método, comprobante, fecha |
| Clases | CU03, CU04 | nombre, fecha, hora de inicio y fin, instructor, vehículo, estado |
| Instructores | CU04 | nombre, especialidad, teléfono, disponibilidad |
| Vehículos | CU04 (parcial) | placa, marca, modelo, estado |
| Asistencia | CU05 | clase, asistió, calificación, observaciones |

**Lo que el frontend todavía no tiene** (según su menú de navegación):

- Pantallas para CU06 (reporte de ingresos), CU07 (mantenimiento), CU08 (gestión de paquetes y promociones, que hoy solo se leen), CU09 (reprogramaciones) y CU10 (historial).
- Los roles **Recepcionista** y **Encargado de Flota** (solo existen Propietario, Administrador, Instructor y Alumno).
- Las notificaciones (include de CU01 y CU02).

**Para conectarlo al backend:** reemplazar las funciones que leen y guardan en `localStorage` (`loadState`, `saveState`, `loginUser`) por llamadas `fetch` a `/api/...`. Ojo: el frontend usa nombres con guion bajo (`numero_clases`) y la API devuelve camelCase (`numeroClases`); hay que unificarlos en un solo lado.

## 8. Convenciones

- Paquetes en minúscula y por módulo: `com.sindaco.<modulo>.<capa>`.
- Clases: `Alumno` (entidad), `AlumnoRequest` y `AlumnoResponse` (DTO), `AlumnoRepository`, `AlumnoService`, `AlumnoController`.
- Rutas REST en plural y minúscula: `/api/alumnos`.
- Un caso de uso = un método de servicio con nombre claro (por ejemplo `registrarMatricula`).
- Para agregar un caso de uso, copiar el patrón del ejemplo `Paquete` (módulo `matricula`).
- Mensajes de error en español, siempre con `ApiError`.

## 9. Hoja de ruta

- [x] Estructura del proyecto y configuración base
- [x] Ejemplo completo de un caso de uso (Paquetes)
- [ ] Base de datos: alinear tablas con el modelo lógico (3.5) y físico (3.6) del informe; pasar de `ddl-auto=update` a `validate` con scripts de migración
- [ ] Entidades JPA y repositorios de todo el modelo
- [ ] Seguridad: login con JWT y permisos por rol
- [ ] Servicios y controladores de CU01 a CU10
- [ ] Conectar el frontend al backend
- [ ] Pruebas y manual de usuario

## 10. Equipo y curso

Curso: Análisis y Diseño de Sistemas de Información – UTP · Docente: Fernando Díaz Sánchez · Lima, 2026.
