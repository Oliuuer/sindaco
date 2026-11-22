# Guía de despliegue de SINDACO desde ZIP (Windows)

Esta guía es para quien **descargó el ZIP** del repositorio `https://github.com/Oliuuer/sindaco` y quiere abrir el sistema en el navegador. **No necesita Git**: todo se hace desde PowerShell.

## 1. Descargar y extraer

1. Entrar a `https://github.com/Oliuuer/sindaco`.
2. Clic en **Code → Download ZIP**.
3. Extraer el ZIP. Por defecto queda en `C:\Users\<tu_usuario>\Downloads\sindaco-main`.
4. Abrir PowerShell y entrar a esa carpeta:

```powershell
cd "$env:USERPROFILE\Downloads\sindaco-main"
dir
```

Debe aparecer el archivo `pom.xml`. Si extrajiste el ZIP en otro lugar, cambia la ruta del `cd`.

## 2. Instalar y verificar requisitos

Necesitas instalados:

- **JDK 21**
- **Maven 3.9 o superior**
- **PostgreSQL 14 o superior**

Verifica que estén disponibles:

```powershell
java -version
mvn -version
Get-NetTCPConnection -LocalPort 5432 -State Listen -ErrorAction SilentlyContinue |
  Select-Object LocalAddress,LocalPort,OwningProcess
```

Si PostgreSQL no aparece escuchando, inicia su servicio:

```powershell
Get-Service | Where-Object { $_.Name -like '*postgres*' } | Start-Service
```

Si la base `sindaco` todavía no existe, créala (el comando te pedirá las credenciales de tu instalación; si ya existe, omite este paso):

```powershell
psql -U postgres -c "CREATE DATABASE sindaco;"
```

## 3. Configuración de conexión

El proyecto ya incluye la configuración de conexión en:

```text
src\main\resources\application.properties
```

Si tu instalación de PostgreSQL usa otros valores, edita ese archivo o define las variables de entorno correspondientes antes de compilar. No hace falta tocar ningún otro archivo.

## 4. Compilar

```powershell
mvn clean package
```

Debe terminar con `BUILD SUCCESS` y ejecutar las 9 pruebas. Si falla, vuelve al paso 2.

## 5. Arrancar la aplicación

### En primer plano (recomendado la primera vez)

Verás los mensajes de arranque en la misma ventana. Se detiene con `Ctrl+C`.

```powershell
java -jar target\sindaco-0.0.1-SNAPSHOT.jar
```

### En segundo plano (opcional)

```powershell
$jar = "$PWD\target\sindaco-0.0.1-SNAPSHOT.jar"
Start-Process -FilePath "java" -ArgumentList "-jar","`"$jar`"" -WindowStyle Hidden `
  -RedirectStandardOutput "$env:TEMP\sindaco-app.out" `
  -RedirectStandardError  "$env:TEMP\sindaco-app.err" | Out-Null

for ($i = 0; $i -lt 40; $i++) {
  if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) { break }
  Start-Sleep -Seconds 1
}
curl.exe -s -o NUL -w "HTTP %{http_code}`n" "http://localhost:8080/"
```

Debe imprimir `HTTP 200`.

## 6. Abrir en el navegador

Abre **`http://localhost:8080/`** en Chrome o Edge.

Inicia sesión con una de estas cuentas. La contraseña del sistema es `pass123`:

| Correo | Rol | Qué ve |
|---|---|---|
| `admin@sindaco.com` | Administrador | Dashboard global y toda la gestión |
| `alumno@sindaco.com` | Alumno | Solo su portal: Mis matrículas, Mis clases y Mis pagos |

También puedes probar: `propietario@sindaco.com`, `recepcionista@sindaco.com`, `encargado@sindaco.com` e `instructor@sindaco.com`.

### Cómo comprobar que el portal del alumno funciona

Entra con `alumno@sindaco.com` y elige el rol **Alumno**. El menú debe mostrar solamente:

- Dashboard
- Mis matrículas
- Mis clases
- Mis pagos
- Historial
- Notificaciones

**No** deben aparecer Alumnos, Matrículas, Pagos, Clases ni Ausencias. En **Mis pagos** se ve la fecha de cada pago realizado; en **Mis clases** se ve el horario: fecha, hora de inicio y fin, instructor y vehículo. El alumno puede matricularse en un paquete desde **Mis matrículas**, pero no ve el listado global ni el botón para guardar alumnos.

## 7. Detener la aplicación

Si la iniciaste en primer plano, pulsa `Ctrl+C`.

Si la iniciaste en segundo plano:

```powershell
Get-CimInstance Win32_Process -Filter "name='java.exe'" |
  Where-Object { $_.CommandLine -like '*sindaco-0.0.1-SNAPSHOT.jar*' } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
```

## 8. Problemas comunes

| Situación | Solución |
|---|---|
| `mvn` no se reconoce | Instala Maven 3.9+ y abre una PowerShell nueva. |
| `java` no se reconoce | Instala JDK 21 y abre una PowerShell nueva. |
| `BUILD FAILURE` al compilar | Comprueba JDK 21, Maven y que PostgreSQL esté activo. |
| No conecta con la base | Verifica el servicio de PostgreSQL y la configuración del paso 3. |
| El puerto 8080 está ocupado | Arranca con `java -jar target\sindaco-0.0.1-SNAPSHOT.jar --server.port=8081` y abre `http://localhost:8081/`. |
| No puede borrar el JAR al recompilar | Detén la aplicación (paso 7) antes de `mvn clean package`. |
| La página muestra `401` o `403` | Inicia sesión con el rol correcto. El alumno solo ve sus propios datos. |

## 9. Resumen para copiar y pegar

```powershell
cd "$env:USERPROFILE\Downloads\sindaco-main"
java -version
mvn -version
Get-NetTCPConnection -LocalPort 5432 -State Listen -ErrorAction SilentlyContinue
mvn clean package
java -jar target\sindaco-0.0.1-SNAPSHOT.jar
```

Luego abre **`http://localhost:8080/`** e inicia sesión con `admin@sindaco.com` o `alumno@sindaco.com`.
