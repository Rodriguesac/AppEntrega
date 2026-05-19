@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo.
echo ==========================================================
echo  UP ENTREGAS - APLICAR APP NATIVO TEMA CLARO
echo ==========================================================
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
echo Copiando app e arquivos Gradle...
echo.

if exist "%DESTINO%\app" (
  echo Fazendo backup rapido da pasta app atual...
  if exist "%DESTINO%\app_backup_up_antigo" rmdir /s /q "%DESTINO%\app_backup_up_antigo"
  move "%DESTINO%\app" "%DESTINO%\app_backup_up_antigo" >nul
)

robocopy "%ORIGEM%app" "%DESTINO%\app" /E /NFL /NDL /NJH /NJS /NP >nul
copy /Y "%ORIGEM%settings.gradle.kts" "%DESTINO%\settings.gradle.kts" >nul
copy /Y "%ORIGEM%build.gradle.kts" "%DESTINO%\build.gradle.kts" >nul
copy /Y "%ORIGEM%gradle.properties" "%DESTINO%\gradle.properties" >nul

if not exist "%DESTINO%\.github\workflows" mkdir "%DESTINO%\.github\workflows"
copy /Y "%ORIGEM%.github\workflows\android-debug.yml" "%DESTINO%\.github\workflows\android-debug.yml" >nul

echo.
echo Pronto. O app novo foi aplicado ao repositorio.
echo.
echo Para gerar APK no GitHub:
echo 1. Envie os arquivos para o GitHub.
echo 2. Abra Actions.
echo 3. Rode "Build Android Debug APK".
echo.
pause
