# SINDACO – Sistema web de gestión de la Escuela de Manejo

Sistema integral para gestionar **matrículas, pagos, comprobantes, clases, instructores, vehículos, asistencia, evaluaciones, mantenimiento, ausencias/reprogramaciones, reportes de ingresos e historial** de una escuela de manejo.

> **Estado: backend y frontend completos.** API REST con arquitectura N capas, autenticación JWT con 6 roles, PostgreSQL y frontend responsive conectado a la API (sin datos simulados).

## 1. Tecnologías

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.0.x (Spring MVC, API REST) |
| Persistencia | Spring Data JPA + Hibernate, consultas JPQL |
| Base de datos | PostgreSQL 14+ |
| Seguridad | Spring Security + JWT (HMAC-SHA256), 6 roles |
| Validación | Jakarta Bean Validation |
| Construcción | Maven 3.9+ |
| Frontend | HTML5 + CSS3 + JavaScript (vanilla, `fetch`) |
| Documentación | [Arquitectura](docs/ARQUITECTURA.md) · [Contrato API](docs/CONTRATO_API.md) |

## 2. Requisitos previos

- JDK 21
- Maven 3.9+
- PostgreSQL 14 o superior

## 3. Cómo ejecutar

1. Crear la base de datos (si no existe):
   ```sql
   CREATE DATABASE sindaco;
   ```
2. Configurar credenciales si hace falta. Por defecto: `postgres / oli` en `localhost:5432`.
   Variables opcionales: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`, `CORS_ORIGINS` (ver `.env.example`).
3. Compilar y ejecutar:
   ```bash
   mvn clean package
   mvn spring-boot:run
   ```
   O ejecutar el JAR: `java -jar target/sindaco-0.0.1-SNAPSHOT.jar`
4. Abrir `http://localhost:8080/` e iniciar sesión.

### Credenciales de prueba (contraseña `pass123`)

| Correo | Rol |
|---|---|
| propietario@sindaco.com | Propietario |
| admin@sindaco.com | Administrador |
| recepcionista@sindaco.com | Recepcionista |
| encargado@sindaco.com | Encargado de Flota |
| instructor@sindaco.com | Instructor |
| alumno@sindaco.com | Alumno |

El menú de navegación y los permisos de la API se aplican según el rol.

Al arrancar por primera vez, `DataSeeder` crea usuarios y datos de ejemplo (paquetes, alumnos, matrículas, pagos, instructores, vehículos, clases, asistencias, mantenimientos y ausencias). Si ya existen datos, no los duplica.

## 4. Arquitectura

**Arquitectura N capas por módulo**. La descripción completa está en [`docs/ARQUITECTURA.md`](docs/ARQUITECTURA.md).

```
Frontend (index.html + app.js + estilo.css)   ← Capa 1 · Presentación
        │ fetch /api/... (JSON + JWT)
        ▼
Controller  → valida @Valid y delega          ← Capa 1 · Presentación
        ▼
Service     → reglas de negocio, transacciones← Capa 2 · Aplicación
        ▼
Repository  → JPA / JPQL                      ← Capa 4 · Persistencia
        ▼
Entity      → modelo de datos                 ← Capa 3 · Dominio

Config, Security (JWT), excepciones y seeder  ← Capa 5 · Infraestructura
```

Reglas principales:

1. Controlador sin lógica de negocio.
2. Entidades nunca salen al frontend: `...Request` / `...Response`.
3. Consultas JPQL en el repositorio correspondiente.
4. Errores uniformes con `ApiError` (404 / 400 / 409 / 401 / 403).
5. Un módulo no accede a repositorios de otro: usa su servicio.

## 5. Estructura de carpetas

```
sindaco/
├── pom.xml
├── docs/
│   ├── ARQUITECTURA.md       Arquitectura N capas y reglas
│   └── CONTRATO_API.md       Todos los endpoints y DTOs
└── src/main/
    ├── java/com/sindaco/
    │   ├── SindacoApplication.java
    │   ├── config/            SecurityConfig, JwtService, JwtAuthenticationFilter, DataSeeder
    │   ├── comun/             ApiError, excepciones, GlobalExceptionHandler
    │   ├── seguridad/         Usuario, Rol, AuthService, UsuarioService, controllers
    │   ├── matricula/         Alumno, Matricula, Pago, Paquete (CU01, CU02, CU08)
    │   ├── agenda/            Clase, Instructor, Vehiculo (CU03, CU04)
    │   ├── academico/         AsistenciaEvaluacion, Reprogramacion, Reportes, Historial (CU05, CU06, CU09, CU10)
    │   └── mantenimiento/     Mantenimiento (CU07)
    └── resources/
        ├── application.properties
        └── static/            index.html, app.js, estilo.css
```

## 6. Módulos y casos de uso

| Módulo | Caso de uso | Endpoint principal | Estado |
|---|---|---|---|
| seguridad | Inicio de sesión y roles · JWT | `/api/auth/login`, `/api/auth/me` | ✅ |
| matricula | CU01 Registrar Matrícula | `/api/alumnos`, `/api/matriculas` | ✅ |
| matricula | CU02 Registrar Pago y Comprobante | `/api/pagos` | ✅ |
| matricula | CU08 Paquetes y promociones | `/api/paquetes` | ✅ |
| agenda | CU03 Programar Clases | `/api/clases` | ✅ |
| agenda | CU04 Asignar Instructor y Vehículo | `/api/instructores`, `/api/vehiculos` | ✅ |
| academico | CU05 Asistencia y Evaluación | `/api/asistencias` | ✅ |
| academico | CU09 Ausencias y Reprogramaciones | `/api/reprogramaciones` | ✅ |
| academico | CU10 Historial Académico y Financiero | `/api/historial/alumnos/{id}` | ✅ |
| academico | CU06 Reporte de Ingresos | `/api/reportes/ingresos` | ✅ |
| mantenimiento | CU07 Mantenimiento de Vehículos | `/api/mantenimientos` | ✅ |

El detalle de todos los endpoints, cuerpos y respuestas está en [`docs/CONTRATO_API.md`](docs/CONTRATO_API.md).

## 7. Frontend

Aplicación de una página responsive con 13 secciones: Dashboard, Alumnos, Matrículas, Pagos, Paquetes, Clases, Vehículos, Instructores, Asistencia, Mantenimiento, Ausencias, Reportes e Historial.

- **Sin datos simulados**: todas las operaciones usan `fetch` contra la API REST.
- **Autenticación**: guarda el JWT en `localStorage` y lo envía en cada petición.
- **Roles**: oculta secciones no permitidas y la API valida los permisos.
- **CRUD completo** en todas las secciones, con formularios reutilizables para crear/editar, confirmación al eliminar y notificaciones.
- **Reportes** con filtro de fechas y **historial** por alumno.
- **Comprobantes de pago** consultables desde el listado de pagos.

## 8. Pruebas rápidas de la API

```bash
# Login (devuelve token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@sindaco.com","password":"pass123","rol":"ADMINISTRADOR"}'

# Listar alumnos (reemplazar TOKEN)
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/alumnos

# Reporte de ingresos
curl -H "Authorization: Bearer TOKEN" "http://localhost:8080/api/reportes/ingresos"
```

## 9. Convenciones

- Paquetes en minúscula: `com.sindaco.<modulo>.<capa>`.
- Clases: `Alumno` (entidad), `AlumnoRequest`/`AlumnoResponse` (DTO), `AlumnoRepository`, `AlumnoService` + `AlumnoServiceImpl`, `AlumnoController`.
- Rutas REST plurales en minúscula; un caso de uso = un método de servicio.
- Mensajes de error y etiquetas en español; codificación UTF-8.
