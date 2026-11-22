# SINDACO – Sistema web de gestión de la Escuela de Manejo

Sistema integral para gestionar **matrículas, pagos, comprobantes, promociones, clases, instructores, vehículos, asistencia, evaluaciones, mantenimiento, ausencias/reprogramaciones, notificaciones, reportes de ingresos e historial** de una escuela de manejo.

> **Estado: backend y frontend completos.** API REST con arquitectura N capas, autenticación JWT con 6 roles, PostgreSQL y frontend responsive conectado a la API (sin datos simulados). Reglas de negocio de los 10 casos de uso implementadas y verificadas con pruebas automáticas. El rol `ALUMNO` cuenta con un portal propio: mis matrículas, mis clases/horarios y mis pagos realizados.

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
| Pruebas | JUnit 5, Mockito y AssertJ |
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

El menú de navegación y la API aplican la misma matriz de permisos por rol. El usuario alumno queda vinculado a un alumno de ejemplo; con esa cuenta verá solo **Mis matrículas**, **Mis clases**, **Mis pagos**, **Historial** y **Notificaciones**. El `alumnoId` se toma del token, nunca del formulario, y las rutas `/api/matriculas/mi`, `/api/clases/mi` y `/api/pagos/mi` solo aceptan `ALUMNO`.

Al arrancar por primera vez, `DataSeeder` crea usuarios y datos de ejemplo (paquetes, promociones, alumnos, matrículas, pagos, instructores, vehículos, clases, asistencias, mantenimientos y ausencias). Si ya existen datos, no los duplica.

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
│   ├── CONTRATO_API.md       Todos los endpoints y DTOs
│   └── APF2_SECCION_3.8.docx Avance 3 (patrones MVC y DAO)
└── src/
    ├── main/java/com/sindaco/
    │   ├── SindacoApplication.java
    │   ├── config/            SecurityConfig, JwtService, JwtAuthenticationFilter, DataSeeder
    │   ├── comun/             ApiError, excepciones, GlobalExceptionHandler, notificacion/
    │   ├── seguridad/         Usuario, Rol, AuthService, UsuarioService, controllers
    │   ├── matricula/         Alumno, Matricula, Pago, Paquete, promocion/ (CU01, CU02, CU08)
    │   ├── agenda/            Clase, Instructor, Vehiculo (CU03, CU04)
    │   ├── academico/         Asistencia, Reprogramacion, Reportes, Historial (CU05, CU06, CU09, CU10)
    │   ├── mantenimiento/     Mantenimiento (CU07)
    │   └── patrones/          MVC y DAO reales documentados en la sección 3.8
    ├── main/resources/
    │   ├── application.properties
    │   └── static/            index.html, app.js, estilo.css
    └── test/java/com/sindaco/ Pruebas unitarias de pagos y clases
```

## 6. Módulos y casos de uso

| Módulo | Caso de uso | Endpoint principal | Reglas incluidas |
|---|---|---|---|
| seguridad | Inicio de sesión y roles · JWT | `/api/auth/login`, `/api/auth/me` | 6 roles, matriz de permisos |
| portal alumno | Mis matrículas, clases y pagos | `/api/matriculas/mi`, `/api/clases/mi`, `/api/pagos/mi` | Datos acotados al `alumnoId` del token; dashboard personal |
| matricula | CU01 Registrar Matrícula | `/api/matriculas` | Promoción vigente, precio final, notificación; el alumno se matricula solo a sí mismo |
| matricula | CU02 Registrar Pago y Comprobante | `/api/pagos` | Saldo, pagos fraccionados, estado de matrícula, notificación |
| matricula | CU08 Paquetes y promociones | `/api/paquetes`, `/api/promociones` | Vigencia, descuento por porcentaje |
| agenda | CU03 Programar Clases | `/api/clases` | Solapamientos, instructor disponible, vehículo operativo |
| agenda | CU04 Asignar Instructor y Vehículo | `/api/instructores`, `/api/vehiculos` | Cambio de estado de vehículo |
| academico | CU05 Asistencia y Evaluación | `/api/asistencias` | Nota y observaciones |
| academico | CU09 Ausencias y Reprogramaciones | `/api/reprogramaciones` | Disponibilidad, mover clase, notificación |
| academico | CU10 Historial Académico y Financiero | `/api/historial/alumnos/{id}`, `/api/historial/mi` | Acceso del alumno a su propio historial |
| academico | CU06 Reporte de Ingresos | `/api/reportes/ingresos` | Exportación CSV y notificación al MTC |
| mantenimiento | CU07 Mantenimiento de Vehículos | `/api/mantenimientos` | Vehículo en taller / operativo |
| comun | Notificaciones | `/api/notificaciones` | Registro y lectura de avisos |

El detalle de todos los endpoints, cuerpos y respuestas está en [`docs/CONTRATO_API.md`](docs/CONTRATO_API.md).

### Matriz de permisos

| Caso de uso | Roles |
|---|---|
| Portal del alumno (`/api/*/mi`) | Solo Alumno |
| CU01 Matrículas | Alumno (solo las suyas), Recepcionista, Administrador |
| CU02 Pagos | Administrador; el alumno solo consulta `/api/pagos/mi` |
| CU03 Clases | Administrador, Instructor; el alumno solo consulta `/api/clases/mi` |
| CU04 Instructores y vehículos | Administrador, Instructor, Encargado de Flota |
| CU05 Asistencia | Administrador, Instructor |
| CU06 Reportes | Administrador, Propietario |
| CU07 Mantenimiento | Administrador, Instructor, Encargado de Flota |
| CU08 Paquetes y promociones | Administrador, Recepcionista, Propietario; el alumno puede consultarlos para matricularse |
| CU09 Reprogramaciones | Administrador (el alumno ya no ve esta sección global en el frontend) |
| CU10 Historial | Administrador, Alumno (`/api/historial/mi`) |

## 7. Frontend

Aplicación de una página responsive con **18 secciones**. Las secciones de gestión son Dashboard, Alumnos, Matrículas, Pagos, Paquetes, Promociones, Clases, Vehículos, Instructores, Asistencia, Mantenimiento, Ausencias, Notificaciones, Reportes e Historial; el portal del alumno añade **Mis matrículas**, **Mis clases y horarios** y **Mis pagos realizados**.

- **Sin datos simulados**: todas las operaciones usan `fetch` contra la API REST.
- **Autenticación**: guarda el JWT en `localStorage` y lo envía en cada petición; al iniciar sesión se consume `/api/auth/me` para obtener el `alumnoId`.
- **Roles**: oculta secciones no permitidas y la API valida los permisos. El alumno ya no ve «Alumnos», «Matrículas», «Pagos», «Clases», «Ausencias» ni el dashboard global.
- **Portal del alumno**: su dashboard muestra sus propias matrículas, próximas clases, pagos, total pagado y saldo pendiente; solo llama a `/api/matriculas/mi`, `/api/clases/mi` y `/api/pagos/mi`.
- **Mis matrículas**: permite matricularse en un paquete vigente (y cancelar sus propias matrículas); la API fuerza su `alumnoId` y el estado `POR_PAGAR`.
- **Mis clases**: consulta sus horarios (fecha, hora de inicio/fin, instructor, vehículo y estado) sin exponer los del resto de alumnos.
- **Mis pagos**: consulta la fecha de cada pago realizado, paquete, monto, tipo, saldo pendiente, método y comprobante.
- **CRUD completo** en las secciones de gestión, con formularios reutilizables, confirmación al eliminar y notificaciones. Las vistas del portal son de solo lectura salvo la matrícula/cancelación propia.
- **Matrículas**: selección opcional de código de promoción; muestra precio final, descuento y saldo.
- **Pagos**: muestra tipo (`TOTAL`/`FRACCIONADO`) y saldo pendiente; admite pagos parciales.
- **Reportes**: filtro de fechas y descarga CSV.
- **Historial**: el alumno consulta `/api/historial/mi`; los demás roles eligen el alumno.
- **Comprobantes de pago** consultables desde el listado de pagos de administración.

## 8. Pruebas

```bash
mvn test
```

Las pruebas unitarias (`src/test/java`) cubren:

- `PagoServiceImplTest`: pago parcial con saldo, pago total que confirma la matrícula, rechazo de montos que superan el saldo y recálculo del estado al eliminar un pago.
- `ClaseServiceImplTest`: solapamiento de instructor/vehículo, vehículo en taller, instructor no disponible, creación válida y validación reutilizable de disponibilidad.

También puede comprobarse el empaquetado completo con `mvn clean package` (ejecuta las pruebas y genera el JAR).

## 9. Pruebas rápidas de la API

```bash
# Login (devuelve token)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin@sindaco.com","password":"pass123","rol":"ADMINISTRADOR"}'

# Listar alumnos (reemplazar TOKEN)
curl -H "Authorization: Bearer TOKEN" http://localhost:8080/api/alumnos

# Reporte de ingresos en CSV
curl -H "Authorization: Bearer TOKEN" "http://localhost:8080/api/reportes/ingresos?formato=csv" -o reporte-ingresos.csv
```

## 10. Convenciones

- Paquetes en minúscula: `com.sindaco.<modulo>.<capa>`.
- Clases: `Alumno` (entidad), `AlumnoRequest`/`AlumnoResponse` (DTO), `AlumnoRepository`, `AlumnoService` + `AlumnoServiceImpl`, `AlumnoController`.
- Rutas REST plurales en minúscula; un caso de uso = un método de servicio.
- Mensajes de error y etiquetas en español; codificación UTF-8.
