# Contrato de la API REST de SINDACO

Todas las rutasresponden JSON. Las peticiones que escriben usan `Content-Type: application/json`.
Las rutas `/api/**` (excepto `/api/auth/login`) requieren el header `Authorization: Bearer <token>`.
Los errores usan siempre el formato `ApiError`: `{ estado, mensaje, detalles[], fecha }`.
Las fechas van en ISO (`yyyy-MM-dd`) y las horas en `HH:mm`.

## Seguridad

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| POST | `/api/auth/login` | `LoginRequest { username, password, rol? }` → `LoginResponse { token, username, nombre, rol }` |
| GET | `/api/auth/me` | → `UsuarioResponse { id, username, nombre, rol }` |
| GET | `/api/usuarios` | → `[UsuarioResponse]` |
| POST | `/api/usuarios` | `UsuarioRequest { username, password, nombre, rol }` → `UsuarioResponse` |
| DELETE | `/api/usuarios/{id}` | 204 |

Roles: `PROPIETARIO`, `ADMINISTRADOR`, `RECEPCIONISTA`, `ENCARGADO_FLOTAS`, `INSTRUCTOR`, `ALUMNO`.

## Matrícula

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| GET | `/api/alumnos` | → `[AlumnoResponse { id, nombre, apellidos, dni, email, telefono, direccion, fechaRegistro }]` |
| GET | `/api/alumnos/{id}` | → `AlumnoResponse` |
| POST | `/api/alumnos` | `AlumnoRequest { nombre, apellidos, dni, email, telefono, direccion, fechaRegistro? }` → `AlumnoResponse` |
| PUT | `/api/alumnos/{id}` | `AlumnoRequest` → `AlumnoResponse` |
| DELETE | `/api/alumnos/{id}` | 204 |
| GET | `/api/paquetes?soloVigentes=false` | → `[PaqueteResponse { id, nombre, categoria, numeroClases, precio, vigente }]` |
| GET | `/api/paquetes/{id}` | → `PaqueteResponse` |
| POST | `/api/paquetes` | `PaqueteRequest { nombre, categoria, numeroClases, precio }` → `PaqueteResponse` |
| PUT | `/api/paquetes/{id}` | `PaqueteRequest` → `PaqueteResponse` |
| DELETE | `/api/paquetes/{id}` | 204 (baja lógica: `vigente=false`) |
| GET | `/api/matriculas` | → `[MatriculaResponse { id, alumnoId, alumnoNombre, paqueteId, paquete, estado, fecha }]` |
| GET | `/api/matriculas/{id}` | → `MatriculaResponse` |
| POST | `/api/matriculas` | `MatriculaRequest { alumnoId, paqueteId, estado?, fecha? }` → `MatriculaResponse` |
| PUT | `/api/matriculas/{id}` | `MatriculaRequest` → `MatriculaResponse` |
| DELETE | `/api/matriculas/{id}` | 204 |
| GET | `/api/pagos` | → `[PagoResponse { id, matriculaId, alumnoNombre, monto, metodo, comprobante, fecha }]` |
| GET | `/api/pagos/{id}` | → `PagoResponse` |
| POST | `/api/pagos` | `PagoRequest { matriculaId, monto, metodo, comprobante?, fecha? }` → `PagoResponse` (genera comprobante si viene vacío) |
| GET | `/api/pagos/{id}/comprobante` | → `ComprobanteResponse { numero, fecha, alumno, paquete, monto, metodo, matriculaId }` |
| DELETE | `/api/pagos/{id}` | 204 |

Estados de matrícula: `CONFIRMADA`, `POR_PAGAR`, `CANCELADA`, `VENCIDA`.
Métodos de pago: `EFECTIVO`, `YAPE`, `PLIN`, `TRANSFERENCIA`.

## Agenda y flota

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| GET | `/api/instructores` | → `[InstructorResponse { id, nombre, especialidad, telefono, disponible }]` |
| GET | `/api/instructores/{id}` | → `InstructorResponse` |
| POST | `/api/instructores` | `InstructorRequest { nombre, especialidad, telefono, disponible? }` → `InstructorResponse` |
| PUT | `/api/instructores/{id}` | `InstructorRequest` → `InstructorResponse` |
| DELETE | `/api/instructores/{id}` | 204 |
| GET | `/api/vehiculos` | → `[VehiculoResponse { id, placa, marca, modelo, estado }]` |
| GET | `/api/vehiculos/{id}` | → `VehiculoResponse` |
| POST | `/api/vehiculos` | `VehiculoRequest { placa, marca, modelo, estado? }` → `VehiculoResponse` |
| PUT | `/api/vehiculos/{id}` | `VehiculoRequest` → `VehiculoResponse` |
| DELETE | `/api/vehiculos/{id}` | 204 |
| GET | `/api/clases` | → `[ClaseResponse { id, nombre, fecha, horaInicio, horaFin, alumnoId, instructorId, vehiculoId, alumnoNombre, instructorNombre, vehiculoPlaca, estado }]` |
| GET | `/api/clases/{id}` | → `ClaseResponse` |
| POST | `/api/clases` | `ClaseRequest { nombre, fecha, horaInicio, horaFin, alumnoId, instructorId, vehiculoId, estado? }` → `ClaseResponse` |
| PUT | `/api/clases/{id}` | `ClaseRequest` → `ClaseResponse` |
| DELETE | `/api/clases/{id}` | 204 |

Estados de clase: `PROGRAMADA`, `CONFIRMADA`, `REALIZADA`, `CANCELADA`.
Estados de vehículo: `OPERATIVO`, `EN_TALLER`, `INACTIVO`.
Regla de negocio: no se pueden programar dos clases con el mismo instructor o vehículo en el mismo horario.

## Académico y reportes

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| GET | `/api/asistencias` | → `[AsistenciaResponse { id, claseId, claseNombre, alumnoId, alumnoNombre, asistio, calificacion, observaciones, fecha }]` |
| GET | `/api/asistencias/{id}` | → `AsistenciaResponse` |
| POST | `/api/asistencias` | `AsistenciaRequest { claseId, asistio, calificacion, observaciones? }` → `AsistenciaResponse` |
| PUT | `/api/asistencias/{id}` | `AsistenciaRequest` → `AsistenciaResponse` |
| DELETE | `/api/asistencias/{id}` | 204 |
| GET | `/api/reprogramaciones` | → `[ReprogramacionResponse { id, alumnoId, alumnoNombre, claseId, claseNombre, motivo, fecha, nuevaFecha, nuevaHoraInicio, nuevaHoraFin, estado }]` |
| GET | `/api/reprogramaciones/{id}` | → `ReprogramacionResponse` |
| POST | `/api/reprogramaciones` | `ReprogramacionRequest { alumnoId, claseId, motivo, fecha?, nuevaFecha?, nuevaHoraInicio?, nuevaHoraFin?, estado? }` → `ReprogramacionResponse` |
| PUT | `/api/reprogramaciones/{id}` | `ReprogramacionRequest` → `ReprogramacionResponse` |
| DELETE | `/api/reprogramaciones/{id}` | 204 |
| GET | `/api/reportes/ingresos?desde=&hasta=` | → `ReporteIngresosResponse { totalIngresos, totalPagos, promedioPago, porMetodo:[{metodo,monto,cantidad}], porMes:[{mes,monto}] }` |
| GET | `/api/historial/alumnos/{alumnoId}` | → `HistorialAlumnoResponse { alumnoId, alumnoNombre, academico:[{fecha,paquete,estado,clase,asistencia,calificacion}], financiero:[{fecha,estado,monto,comprobante}] }` |

Estados de reprogramación: `PENDIENTE`, `PENDIENTE_REPROGRAMACION`, `REPROGRAMADA`, `JUSTIFICADA`.

## Mantenimiento

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| GET | `/api/mantenimientos` | → `[MantenimientoResponse { id, vehiculoId, vehiculoPlaca, tipo, descripcion, fecha, estado, proveedor, costo }]` |
| GET | `/api/mantenimientos/{id}` | → `MantenimientoResponse` |
| POST | `/api/mantenimientos` | `MantenimientoRequest { vehiculoId, tipo, descripcion, fecha?, estado?, proveedor, costo? }` → `MantenimientoResponse` |
| PUT | `/api/mantenimientos/{id}` | `MantenimientoRequest` → `MantenimientoResponse` |
| DELETE | `/api/mantenimientos/{id}` | 204 |

Tipos: `PREVENTIVO`, `CORRECTIVO`, `INSPECCION`. Estados: `PENDIENTE`, `EN_PROCESO`, `COMPLETADO`.
