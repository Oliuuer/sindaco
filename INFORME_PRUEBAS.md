# Resultados de pruebas de SINDACO

Este documento sigue la estructura del ejemplo **“Muestra pruebas de sistemas.pdf”** (Capítulo 4: Resultados), adaptado al proyecto SINDACO.

**Resumen de la ejecución:** 14 pruebas ejecutadas, 0 fallos, 0 errores, 0 omitidas (`BUILD SUCCESS`).

> **Sobre NetBeans:** el ejemplo usa NetBeans porque es un IDE que ejecuta Maven por debajo. SINDACO es un proyecto **Apache Maven** con **JUnit 5**, así que las mismas pruebas se ejecutan desde PowerShell con `mvn test` o abriendo el `pom.xml` en NetBeans y eligiendo *Test Project*. No es necesario instalar NetBeans.

## 4.1. Resultados

### 4.1.1. Informe de Testing Unitario – Tabla Pagos y Matrículas

**1. Nombre del Proyecto**  
SINDACO – Sistema web de gestión administrativa y operativa de la escuela de manejo.

**2. Objetivo del Informe**  
Validar las reglas de saldo, tipo de pago y cambio de estado de la matrícula, asegurando que los importes y las restricciones de integridad se cumplan antes de guardar en la base de datos.

**3. Entorno de Pruebas**

- Base de Datos: PostgreSQL 14 o superior (las pruebas unitarias usan objetos simulados, no requieren base).
- Entorno de Desarrollo: Apache Maven 3.9+ y JDK 21 (compatible con NetBeans).
- Herramienta de Testing: JUnit 5, Mockito y AssertJ.
- Versión del Sistema: V1.0.

**4. Casos de Prueba de la Tabla Pagos**

| ID de Prueba | Función | Descripción | Datos de Entrada | Resultado Esperado | Resultado Obtenido | Estado |
|---|---|---|---|---|---|---|
| P-01 | Pago parcial | Registrar un abono menor al precio de la matrícula | `matriculaId=100`, `monto=300.00`, `metodo=YAPE`, precio `450.00` | Saldo `150.00`, tipo `FRACCIONADO`, matrícula `POR_PAGAR` | Saldo `150.00`, tipo `FRACCIONADO`, matrícula `POR_PAGAR`, notificación registrada | Aprobado |
| P-02 | Pago total | Registrar un pago que cancela el saldo | `matriculaId=100`, `monto=450.00`, `metodo=YAPE` | Saldo `0.00`, tipo `TOTAL`, matrícula `CONFIRMADA` | Saldo `0.00`, tipo `TOTAL`, matrícula `CONFIRMADA` | Aprobado |
| P-03 | Validar saldo | Rechazar un pago mayor al saldo pendiente | Saldo `50.00`, `monto=200.00` | Excepción de regla de negocio, no se guarda el pago | `ReglaNegocioException` (“supera el saldo pendiente”), `save` nunca se llama | Aprobado |
| P-04 | Eliminar pago | Recalcular el estado de la matrícula al eliminar un pago | Pago `450.00` eliminado, total restante `200.00` | Matrícula vuelve a `POR_PAGAR` | Matrícula `POR_PAGAR` y se guarda el cambio | Aprobado |

**5. Resultados de la Prueba**  
Las cuatro pruebas unitarias confirmaron que los abonos parciales dejan saldo y mantienen la matrícula pendiente, que el pago total la confirma, que no se permite pagar más de lo debido y que al eliminar un pago se recalcula el estado.

**6. Conclusiones del Informe**  
La lógica de pagos cumple con las reglas de negocio y conserva la consistencia entre importes y estados. Ningún caso permitió guardar un pago inválido.

---

### 4.1.2. Informe de Testing Unitario – Tabla Clases

**1. Nombre del Proyecto**  
SINDACO – Sistema web de gestión administrativa y operativa de la escuela de manejo.

**2. Objetivo del Informe**  
Validar la disponibilidad de instructores y vehículos al programar clases, evitando solapamientos y asegurando que solo se guarden clases válidas.

**3. Entorno de Pruebas**

- Base de Datos: PostgreSQL 14 o superior (las pruebas unitarias usan objetos simulados).
- Entorno de Desarrollo: Apache Maven 3.9+ y JDK 21 (compatible con NetBeans).
- Herramienta de Testing: JUnit 5, Mockito y AssertJ.
- Versión del Sistema: V1.0.

**4. Casos de Prueba de la Tabla Clases**

| ID de Prueba | Función | Descripción | Datos de Entrada | Resultado Esperado | Resultado Obtenido | Estado |
|---|---|---|---|---|---|---|
| C-01 | Validar horario | Rechazar clase cuando instructor o vehículo ya tienen clase | Fecha `2026-12-01`, `10:00–11:30`, instructor `2`, vehículo `3`, solapamiento `true` | Conflicto, no se guarda la clase | `ConflictoException` (“ya tienen una clase asignada”), `save` nunca se llama | Aprobado |
| C-02 | Validar vehículo | Rechazar un vehículo que está en taller | `estadoVehiculo=EN_TALLER` | Excepción de regla de negocio | `ReglaNegocioException` (“no está operativo”), no se guarda | Aprobado |
| C-03 | Validar instructor | Rechazar un instructor no disponible | `disponible=false` | Excepción de regla de negocio | `ReglaNegocioException` (“no está disponible”) | Aprobado |
| C-04 | Crear clase | Programar una clase sin conflictos | Fecha `2026-12-01`, `10:00–11:30`, vehículo operativo e instructor disponible | Clase `PROGRAMADA`, alumno “Ana Torres” | Estado `PROGRAMADA`, fecha `2026-12-01`, alumno “Ana Torres”, guardada | Aprobado |
| C-05 | Consultar disponibilidad | Validar horario sin persistir cambios | Misma clase, sin solapamiento | No se guarda nada | `save` nunca se llama | Aprobado |

**5. Resultados de la Prueba**  
Las cinco pruebas confirmaron que se bloquean los solapamientos, los vehículos en taller y los instructores no disponibles, y que una clase válida se registra como `PROGRAMADA`.

**6. Conclusiones del Informe**  
La agenda mantiene la disponibilidad de recursos y evita reservas inválidas. La consulta de disponibilidad es de solo lectura: no modifica la base de datos.

---

### 4.1.3. Informe de Pruebas de Integración – Tablas Alumno, Matrícula y Pago

**1. Nombre del Proyecto**  
SINDACO – Sistema web de gestión administrativa y operativa de la escuela de manejo.

**2. Objetivo del Informe**  
Validar que las tablas Alumno, Matrícula y Pago interactúen correctamente: que los pagos se reflejen en el saldo y estado de la matrícula, que los comprobantes se generen y que no se registren matrículas de alumnos inexistentes.

**3. Entorno de Pruebas**

- Base de Datos: H2 en memoria (aislada de PostgreSQL) con el esquema generado por Hibernate.
- Entorno de Desarrollo: Apache Maven 3.9+ y JDK 21 (compatible con NetBeans).
- Herramienta de Testing: JUnit 5, Spring Boot Test (contexto real) y AssertJ.
- Versión del Sistema: V1.0.

**4. Casos de Prueba de Integración**

| ID de Prueba | Función | Descripción | Datos de Entrada | Resultado Esperado | Resultado Obtenido | Estado |
|---|---|---|---|---|---|---|
| PI-01 | Registrar alumno, matrícula y abono | Verificar que el pago parcial se refleje en las tres tablas | Alumno DNI `11111111`, paquete `450.00`, matrícula `2026-01-10`, pago `300.00` | Saldo `150.00`, tipo `FRACCIONADO`, comprobante `B-…`, matrícula `POR_PAGAR` | Saldo `150.00`, comprobante `B-…`, matrícula `POR_PAGAR`, 1 pago listado para el alumno | Aprobado |
| PI-02 | Pago total y comprobante | Cancelar el saldo y consultar el comprobante | Pago `450.00`, método `EFECTIVO` | Saldo `0.00`, matrícula `CONFIRMADA`, comprobante con alumno y paquete | Saldo `0.00`, tipo `TOTAL`, matrícula `CONFIRMADA`, comprobante “Ana Torres” / “Paquete 20 clases” | Aprobado |
| PI-03 | Eliminar pago | Verificar que la matrícula vuelva a pendiente | Pago `450.00` eliminado | Matrícula `POR_PAGAR` y sin pagos asociados | Matrícula `POR_PAGAR`, `listarPorMatricula` vacío | Aprobado |
| PI-04 | Integridad referencial | Evitar una matrícula con alumno inexistente | `alumnoId=999` (no existe), paquete vigente | Excepción de recurso no encontrado, sin filas nuevas | `RecursoNoEncontradoException` (“Alumno”), contadores de matrículas y pagos sin cambios | Aprobado |
| PI-05 | Portal del alumno | Comprobar que cada alumno ve solo sus pagos | Dos alumnos con pagos de `100.00` y `200.00` | Cada consulta devuelve únicamente el pago de ese alumno | Alumno 1: 1 pago de `100.00`; Alumno 2: 1 pago de `200.00` | Aprobado |

**5. Observación y Corrección**  
Todas las pruebas de integración fueron aprobadas. Los resultados confirman que:

- Los pagos se vinculan correctamente con su matrícula y con su alumno.
- El saldo y el estado de la matrícula se recalculan al crear o eliminar pagos.
- Los comprobantes se generan automáticamente y muestran los datos del alumno y del paquete.
- No se registran matrículas cuyo alumno no existe, protegiendo la coherencia de los datos.
- El portal del alumno devuelve únicamente la información perteneciente al alumno autenticado.

**6. Conclusión del Informe**  
La integración entre Alumno, Matrícula y Pago cumple con los requisitos de integridad y funcionalidad definidos. Las relaciones entre las tablas funcionan correctamente y los datos se reflejan de forma coherente en la base de datos y en el portal del alumno.

## Cómo ejecutar las pruebas y obtener la evidencia

Desde la carpeta del proyecto, en PowerShell:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\ejecutar_pruebas.ps1
```

O directamente:

```powershell
mvn test
```

### Desde NetBeans (IDE)

1. Abre NetBeans y elige **File → Open Project…**, selecciona la carpeta `sindaco-main` y pulsa **Open Project**.
2. En la ventana **Projects** (`Ctrl+1`), selecciona el nodo **sindaco**.
3. Pulsa **Alt+F6** o elige **Run → Test Project (sindaco)**.
4. Al terminar aparece el aviso *Tests completed successfully for project: sindaco*. Abre la ventana **Test Results** para ver el detalle por caso.

> NetBeans ejecuta el mismo `mvn test` de Apache Maven, así que los resultados son idénticos a los de PowerShell.

Al terminar debe aparecer:

```text
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

El detalle de cada caso queda en `target\surefire-reports\*.txt`. Para generar el paquete completo (compila y vuelve a ejecutar las pruebas):

```powershell
mvn clean package
```
