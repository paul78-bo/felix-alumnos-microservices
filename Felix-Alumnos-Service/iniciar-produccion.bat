@echo off
title FELIX ALUMNOS SERVICE - PRODUCCIÓN
color 0A

echo ========================================
echo   🚀 FELIX - MODO PRODUCCIÓN
echo ========================================
echo.

:: Verificar que existe el archivo de configuración
if not exist "config\application-prod.properties" (
    echo ❌ ERROR: No existe config\application-prod.properties
    echo.
    pause
    exit /b 1
)

:: Detener instancia anterior si existe
echo ⏹️  Deteniendo instancia anterior...
for /f "tokens=5" %%a in ('netstat -ano ^| findstr :8081 ^| findstr LISTENING') do (
    taskkill /F /PID %%a 2>nul
)
timeout /t 2 /nobreak >nul

:: Establecer variables de entorno
set SMTP_PASSWORD=tkchgatwtdrrwfsf
set DB_PASSWORD=felix123
:: ✅ CAMBIADO: Agregar puerto 8080
set APP_FRONTEND_URL=http://control-escolar.com:8080

:: Empaquetar si es necesario
echo 📦 Verificando compilación...
if not exist "target\Felix-Alumnos-Service-0.0.1-SNAPSHOT.jar" (
    echo Compilando aplicación...
    call mvn clean package -DskipTests
)

:: Iniciar con perfil de producción
echo 🌐 Iniciando servicio en PRODUCCIÓN...
echo URL Frontend: %APP_FRONTEND_URL%
echo.

java -jar target/Felix-Alumnos-Service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod

if errorlevel 1 (
    echo ❌ Error al iniciar la aplicación
    pause
    exit /b 1
)