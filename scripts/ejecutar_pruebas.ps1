# Ejecuta todas las pruebas de SINDACO y muestra el resumen por clase.
# Uso: .\scripts\ejecutar_pruebas.ps1

$ErrorActionPreference = "Stop"

Write-Host "Ejecutando pruebas con Apache Maven..." -ForegroundColor Cyan
mvn test

if ($LASTEXITCODE -ne 0) {
    Write-Host "Las pruebas terminaron con errores (codigo $LASTEXITCODE)." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Resumen por clase de prueba:" -ForegroundColor Cyan
Get-ChildItem "target\surefire-reports\*.txt" | ForEach-Object {
    Select-String -Path $_.FullName -Pattern "^Tests run:" |
        ForEach-Object { "{0}: {1}" -f $_.Filename, $_.Line.Trim() }
}

Write-Host ""
Write-Host "Detalle completo en target\surefire-reports\" -ForegroundColor Green
