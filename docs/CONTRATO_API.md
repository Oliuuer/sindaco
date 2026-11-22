# Contrato de la API REST de SINDACO

Todas las rutas responden JSON, salvo la exportación CSV. Las peticiones que escriben usan `Content-Type: application/json`.
Las rutas `/api/**` (excepto `/api/auth/login`) requieren el header `Authorization: Bearer <token>`.
Los errores usan siempre el formato `ApiError`: `{ estado, mensaje, detalles[], fecha }`.
Las fechas van en ISO (`yyyy-MM-dd`) y las horas en `HH:mm`.

## Matriz de permisos por caso de uso

| Caso de uso | Rutas | Roles con acceso |
|---|---|---|
| CU01 Registrar matrícula | `/api/matriculas/**` | ALUMNO, RECEPCIONISTA, ADMINISTRADOR (el ALUMNO solo ve/se matricula con sus datos) |
| Portal del alumno | `/api/matriculas/mi`, `/api/clases/mi`, `/api/pagos/mi` | SOLO ALUMNO |
| CU02 Pago y comprobante | `/api/pagos/**` | ADMINISTRADOR (excepto `/api/pagos/mi`, solo ALUMNO) |
| CU03 Programar clases | `/api/clases/**` | ADMINISTRADOR, INSTRUCTOR (excepto `/api/clases/mi`, solo ALUMNO) |
| CU04 Instructor y vehículo | `/api/instructores/**`, `/api/vehiculos/**` | ADMINISTRADOR, INSTRUCTOR, ENCARGADO_FLOTAS |
| CU05 Asistencia y evaluación | `/api/asistencias/**` | ADMINISTRADOR, INSTRUCTOR |
| CU06 Reporte de ingresos | `/api/reportes/**` | ADMINISTRADOR, PROPIETARIO |
| CU07 Mantenimiento | `/api/mantenimientos/**` | ADMINISTRADOR, INSTRUCTOR, ENCARGADO_FLOTAS |
| CU08 Paquetes y promociones | `/api/paquetes/**`, `/api/promociones/**` | ADMINISTRADOR, RECEPCIONISTA, PROPIETARIO (el ALUMNO solo puede consultarlos para matricularse) |
| CU09 Ausencias y reprogramaciones | `/api/reprogramaciones/**` | ADMINISTRADOR, ALUMNO |
| CU10 Historial | `/api/historial/**` | ADMINISTRADOR, ALUMNO |
| Apoyo | `/api/alumnos/**` | ADMINISTRADOR, RECEPCIONISTA, INSTRUCTOR (el ALUMNO ya no accede al listado global) |
| Notificaciones | `/api/notificaciones/**` | cualquier usuario autenticado |
| Usuarios | `/api/usuarios/**` | ADMINISTRADOR, PROPIETARIO |

Una petición con un rol no autorizado responde `403` con `ApiError`. Un `AccessDeniedException` de propiedad (por ejemplo, consultar la matrícula de otro alumno) también se traduce a `403`.

## Portal del alumno

El rol `ALUMNO` tiene un portal propio en lugar de las secciones globales. El `alumnoId` se toma siempre del usuario autenticado (`/api/auth/me`), nunca del cuerpo de la petición.

| Método | Ruta | Respuesta | Uso en el frontend |
|---|---|---|---|
| GET | `/api/matriculas/mi` | `[MatriculaResponse]` del alumno autenticado | «Mis matrículas» y resumen del dashboard |
| GET | `/api/clases/mi` | `[ClaseResponse]` ordenado por fecha y hora | «Mis clases y horarios» y dashboard |
| GET | `/api/pagos/mi` | `[PagoResponse]` ordenado por fecha descendente | «Mis pagos realizados» y dashboard |

Reglas del portal:

- Las tres rutas `/mi` están declaradas antes que las reglas generales y solo aceptan `ALUMNO`; cualquier otro rol recibe `403`.
- `GET /api/matriculas` sigue disponible para el alumno, pero devuelve únicamente sus matrículas.
- En `POST /api/matriculas`, el alumno no puede elegir alumno ni estado: el controlador fuerza su `alumnoId` y `POR_PAGAR`.
- En `GET/PUT/DELETE /api/matriculas/{id}`, el controlador verifica que la matrícula pertenezca al alumno; si no, responde `403`.
- El alumno ya no puede consultar `/api/alumnos/**`, `/api/clases/**`, `/api/pagos/**` ni `/api/reportes/**`.
- El dashboard del alumno solo llama a las tres rutas `/mi`; los dashboards operativos siguen usando los indicadores globales.

## Seguridad

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| POST | `/api/auth/login` | `LoginRequest { username, password, rol? }` → `LoginResponse { token, username, nombre, rol }` |
| GET | `/api/auth/me` | → `UsuarioResponse { id, username, nombre, rol, alumnoId }` |
| GET | `/api/usuarios` | → `[UsuarioResponse]` |
| POST | `/api/usuarios` | `UsuarioRequest { username, password, nombre, rol, alumnoId? }` → `UsuarioResponse` |
| DELETE | `/api/usuarios/{id}` | 204 |

Roles: `PROPIETARIO`, `ADMINISTRADOR`, `RECEPCIONISTA`, `ENCARGADO_FLOTAS`, `INSTRUCTOR`, `ALUMNO`.
`alumnoId` vincula la cuenta con un alumno; lo usan `/api/historial/mi` y las tres rutas `/api/*/mi` del portal del alumno.

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
| GET | `/api/matriculas` | → `[MatriculaResponse { id, alumnoId, alumnoNombre, paqueteId, paquete, estado, fecha, precioFinal, descuento, codigoPromocion }]` (el ALUMNO recibe solo las suyas) |
| GET | `/api/matriculas/mi` | → `[MatriculaResponse]` del alumno autenticado (solo ALUMNO) |
| GET | `/api/matriculas/{id}` | → `MatriculaResponse` (el ALUMNO solo si es suya; si no, `403`) |
| POST | `/api/matriculas` | `MatriculaRequest { alumnoId, paqueteId, estado?, fecha?, codigoPromocion? }` → `MatriculaResponse` |
| PUT | `/api/matriculas/{id}` | `MatriculaRequest` → `MatriculaResponse` |
| DELETE | `/api/matriculas/{id}` | 204 |

Estados de matrícula: `CONFIRMADA`, `POR_PAGAR`, `CANCELADA`, `VENCIDA`.
Al matricular con `codigoPromocion` se valida la vigencia de la promoción, se descuenta el porcentaje del precio del paquete y se guardan `precioFinal`, `descuento` (monto descontado) y `codigoPromocion`. Se registra una notificación `MATRICULA`.
Cuando quien matricula es un `ALUMNO`, el controlador ignora el `alumnoId` y el `estado` enviados en el cuerpo: usa su propio `alumnoId` y fuerza `POR_PAGAR`.

### Promociones (CU08)

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| GET | `/api/promociones` | → `[PromocionResponse { id, codigo, descripcion, porcentajeDescuento, fechaInicio, fechaFin, vigente, activaHoy }]` |
| GET | `/api/promociones/{id}` | → `PromocionResponse` |
| GET | `/api/promociones/codigo/{codigo}` | → `PromocionResponse` (rechaza promociones vencidas) |
| POST | `/api/promociones` | `PromocionRequest { codigo, descripcion?, porcentajeDescuento, fechaInicio, fechaFin, vigente? }` → `PromocionResponse` |
| PUT | `/api/promociones/{id}` | `PromocionRequest` → `PromocionResponse` |
| DELETE | `/api/promociones/{id}` | 204 |

### Pagos (CU02)

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| GET | `/api/pagos` | → `[PagoResponse { id, matriculaId, alumnoNombre, monto, metodo, comprobante, fecha, saldoPendiente, tipo }]` |
| GET | `/api/pagos/mi` | → `[PagoResponse]` de las matrículas del alumno autenticado (solo ALUMNO) |
| GET | `/api/pagos/{id}` | → `PagoResponse` |
| POST | `/api/pagos` | `PagoRequest { matriculaId, monto, metodo, comprobante?, fecha? }` → `PagoResponse` (genera comprobante si viene vacío) |
| GET | `/api/pagos/{id}/comprobante` | → `ComprobanteResponse { numero, fecha, alumno, paquete, monto, metodo, matriculaId }` |
| DELETE | `/api/pagos/{id}` | 204 |

Métodos de pago: `EFECTIVO`, `YAPE`, `PLIN`, `TRANSFERENCIA`.
`tipo` es `TOTAL` cuando el abono cancela el saldo y `FRACCIONADO` cuando queda saldo.
Reglas: el monto no puede superar `precioFinal − pagos anteriores`; al llegar el saldo a cero la matrícula pasa a `CONFIRMADA`, y al eliminar un pago se recalcula a `POR_PAGAR` si queda deuda. Cada pago genera una notificación `PAGO`.

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
| GET | `/api/clases/mi` | → `[ClaseResponse]` del alumno autenticado, ordenado por fecha y hora (solo ALUMNO) |
| GET | `/api/clases/{id}` | → `ClaseResponse` |
| POST | `/api/clases` | `ClaseRequest { nombre, fecha, horaInicio, horaFin, alumnoId, instructorId, vehiculoId, estado? }` → `ClaseResponse` |
| PUT | `/api/clases/{id}` | `ClaseRequest` → `ClaseResponse` |
| DELETE | `/api/clases/{id}` | 204 |

Estados de clase: `PROGRAMADA`, `CONFIRMADA`, `REALIZADA`, `CANCELADA`.
Estados de vehículo: `OPERATIVO`, `EN_TALLER`, `INACTIVO`.
Regla de negocio: no se pueden programar dos clases con el mismo instructor o vehículo en el mismo horario; tampoco se puede asignar un vehículo que no esté `OPERATIVO`. El método interno `ClaseService.validarDisponibilidad` reutiliza estas reglas para las reprogramaciones.

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
| GET | `/api/reportes/ingresos?desde=&hasta=&formato=csv` | → `text/csv` con `Content-Disposition: attachment; filename="reporte-ingresos.csv"` |
| GET | `/api/historial/alumnos/{alumnoId}` | → `HistorialAlumnoResponse { alumnoId, alumnoNombre, academico:[{fecha,paquete,estado,clase,asistencia,calificacion}], financiero:[{fecha,estado,monto,comprobante}] }` |
| GET | `/api/historial/mi` | → `HistorialAlumnoResponse` del alumno vinculado al usuario autenticado |

Estados de reprogramación: `PENDIENTE`, `PENDIENTE_REPROGRAMACION`, `REPROGRAMADA`, `JUSTIFICADA`.
Al crear una reprogramación se valida disponibilidad del instructor y vehículo en el nuevo horario, se actualizan la fecha/hora y el estado de la clase, y se registra una notificación `REPROGRAMACION`.
La exportación CSV registra una notificación `MTC` con el detalle del reporte enviado.

## Notificaciones

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| GET | `/api/notificaciones` | → `[NotificacionResponse { id, tipo, titulo, mensaje, fecha, destinatarioRol, alumnoId, leida }]` |
| GET | `/api/notificaciones/rol/{rol}` | → `[NotificacionResponse]` |
| GET | `/api/notificaciones/alumno/{alumnoId}` | → `[NotificacionResponse]` |
| POST | `/api/notificaciones` | `NotificacionRequest { tipo, titulo, mensaje, destinatarioRol?, alumnoId? }` → `NotificacionResponse` (201) |
| PATCH | `/api/notificaciones/{id}/leida` | → 200 / 404 |

Tipos registrados por el sistema: `MATRICULA`, `PAGO`, `REPROGRAMACION`, `MTC`.

## Mantenimiento

| Método | Ruta | Cuerpo / respuesta |
|---|---|---|
| GET | `/api/mantenimientos` | → `[MantenimientoResponse { id, vehiculoId, vehiculoPlaca, tipo, descripcion, fecha, estado, proveedor, costo }]` |
| GET | `/api/mantenimientos/{id}` | → `MantenimientoResponse` |
| POST | `/api/mantenimientos` | `MantenimientoRequest { vehiculoId, tipo, descripcion, fecha?, estado?, proveedor, costo? }` → `MantenimientoResponse` |
| PUT | `/api/mantenimientos/{id}` | `MantenimientoRequest` → `MantenimientoResponse` |
| DELETE | `/api/mantenimientos/{id}` | 204 |

Tipos: `PREVENTIVO`, `CORRECTIVO`, `INSPECCION`. Estados: `PENDIENTE`, `EN_PROCESO`, `COMPLETADO`.
Al pasar a `EN_PROCESO` el vehículo queda `EN_TALLER`; al llegar a `COMPLETADO` vuelve a `OPERATIVO`. Si se elimina un mantenimiento pendiente o en curso, el vehículo se libera.
