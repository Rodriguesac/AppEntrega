@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ==========================================================
echo  UP ENTREGAS - CORRIGIR E APLICAR APP NATIVO TEMA CLARO
echo ==========================================================
echo.
echo Este aplicador remove conflitos antigos de Gradle Groovy/KTS
echo e força o modulo app a virar Android Application.
echo.

set "ORIGEM=%~dp0"
set /p "DESTINO=Digite o caminho da pasta do seu repositorio Android: "

if "%DESTINO%"=="" (
  echo Caminho vazio. Cancelado.
  pause
  exit /b 1
)

if not exist "%DESTINO%" (
  echo A pasta informada nao existe.
  pause
  exit /b 1
)

echo.
echo Limpando arquivos que podem causar conflito...
echo.

if exist "%DESTINO%\app_backup_up_antigo" rmdir /s /q "%DESTINO%\app_backup_up_antigo"
if exist "%DESTINO%\app" move "%DESTINO%\app" "%DESTINO%\app_backup_up_antigo" >nul

if exist "%DESTINO%\settings.gradle" del /f /q "%DESTINO%\settings.gradle"
if exist "%DESTINO%\settings.gradle.kts" del /f /q "%DESTINO%\settings.gradle.kts"
if exist "%DESTINO%\build.gradle" del /f /q "%DESTINO%\build.gradle"
if exist "%DESTINO%\build.gradle.kts" del /f /q "%DESTINO%\build.gradle.kts"
if exist "%DESTINO%\gradle.properties" del /f /q "%DESTINO%\gradle.properties"

echo Copiando app novo e arquivos Gradle corrigidos...
echo.

robocopy "%ORIGEM%app" "%DESTINO%\app" /E /NFL /NDL /NJH /NJS /NP >nul
copy /Y "%ORIGEM%settings.gradle" "%DESTINO%\settings.gradle" >nul
copy /Y "%ORIGEM%settings.gradle.kts" "%DESTINO%\settings.gradle.kts" >nul
copy /Y "%ORIGEM%build.gradle" "%DESTINO%\build.gradle" >nul
copy /Y "%ORIGEM%build.gradle.kts" "%DESTINO%\build.gradle.kts" >nul
copy /Y "%ORIGEM%gradle.properties" "%DESTINO%\gradle.properties" >nul

if not exist "%DESTINO%\.github\workflows" mkdir "%DESTINO%\.github\workflows"
copy /Y "%ORIGEM%.github\workflows\android-debug.yml" "%DESTINO%\.github\workflows\android-debug.yml" >nul

echo.
echo Pronto. Correcao aplicada.
echo.
echo Agora rode na pasta do repositorio:
echo   git status
echo   git add .
echo   git commit -m "Corrigir build Android app Up Entregas"
echo   git push

echo.
echo Depois va no GitHub Actions e rode: Build Android Debug APK
echo.
pause
