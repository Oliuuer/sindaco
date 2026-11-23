# Genera EVIDENCIA_PRUEBAS.html con el porcentaje real de pruebas aprobadas.
# Ejecuta primero: mvn test
# Uso: powershell -ExecutionPolicy Bypass -File .\scripts\generar_evidencia.ps1

$ErrorActionPreference = "Stop"

$reportes = @(Get-ChildItem "target\surefire-reports\TEST-*.xml" -ErrorAction SilentlyContinue)
if ($reportes.Count -eq 0) {
    Write-Host "No hay reportes. Ejecuta 'mvn test' primero." -ForegroundColor Red
    exit 1
}

$total = 0
$fallos = 0
$errores = 0
$omitidas = 0
$filas = ""

foreach ($reporte in $reportes) {
    [xml]$xml = Get-Content $reporte.FullName
    $suite = $xml.testsuite
    $t = [int]$suite.tests
    $f = [int]$suite.failures
    $e = [int]$suite.errors
    $s = [int]$suite.skipped
    $total += $t
    $fallos += $f
    $errores += $e
    $omitidas += $s
    $estado = if ($f -eq 0 -and $e -eq 0) { "OK" } else { "ERROR" }
    $filas += "<tr><td>$($suite.name)</td><td>$t</td><td>$f</td><td>$e</td><td>$s</td><td class=`"ok`">$estado</td></tr>`n"
}

$aprobadas = $total - $fallos - $errores - $omitidas
if ($total -gt 0) {
    $porcentaje = [math]::Round((100 * $aprobadas) / $total, 1)
} else {
    $porcentaje = 0
}

$html = @"
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<title>Evidencia de pruebas SINDACO</title>
<style>
  body { font-family: Segoe UI, Arial, sans-serif; margin: 32px; color: #1f2937; }
  h1 { color: #0f172a; }
  .porcentaje { font-size: 96px; font-weight: 700; color: #16a34a; line-height: 1; }
  .tarjeta { border: 2px solid #16a34a; border-radius: 16px; padding: 24px 32px; display: inline-block; background: #f0fdf4; }
  .detalle { font-size: 22px; }
  table { border-collapse: collapse; margin-top: 28px; width: 100%; }
  th, td { border: 1px solid #cbd5e1; padding: 10px 12px; text-align: left; }
  th { background: #0f172a; color: white; }
  .ok { color: #16a34a; font-weight: 700; }
  small { color: #64748b; }
</style>
</head>
<body>
  <h1>SINDACO - Resultado de pruebas automaticas</h1>
  <div class="tarjeta">
    <div class="porcentaje">$porcentaje%</div>
    <div class="detalle">Pruebas aprobadas: <strong>$aprobadas</strong> de <strong>$total</strong></div>
    <div class="detalle">Fallos: $fallos &nbsp;&#183;&nbsp; Errores: $errores &nbsp;&#183;&nbsp; Omitidas: $omitidas</div>
  </div>
  <h2>Detalle por clase de prueba</h2>
  <table>
    <tr><th>Clase</th><th>Pruebas</th><th>Fallos</th><th>Errores</th><th>Omitidas</th><th>Estado</th></tr>
    $filas
  </table>
  <p><small>Generado desde target/surefire-reports/TEST-*.xml - Apache Maven + JUnit 5.</small></p>
</body>
</html>
"@

$html | Out-File -FilePath "EVIDENCIA_PRUEBAS.html" -Encoding utf8
Write-Host "Porcentaje: $porcentaje%  ($aprobadas de $total)" -ForegroundColor Green
Write-Host "Archivo generado: EVIDENCIA_PRUEBAS.html" -ForegroundColor Green
