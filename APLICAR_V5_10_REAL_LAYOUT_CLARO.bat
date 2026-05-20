@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

echo.
echo ================================================================
echo  UP ENTREGAS / RODRIGUES ENTREGADOR - V5.10 REAL + LAYOUT CLARO
echo ================================================================
echo.
echo Mantem a logica real/Firebase do projeto e aplica a interface nova em tema claro.
echo Corrige tambem o erro AAPT: resource color/white not found.
echo Preserva o app\google-services.json atual se ja existir.
echo.
set /p DEST=Digite o caminho da pasta do seu repositorio Android: 

if "%DEST%"=="" (
  echo Caminho vazio.
  pause
  exit /b 1
)

if not exist "%DEST%" (
  echo Pasta nao encontrada: %DEST%
  pause
  exit /b 1
)

for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set TS=%%i
set SRC=%~dp0
set KEEP=%TEMP%\google-services-v510-!TS!.json

if exist "%DEST%\app\google-services.json" (
  copy /Y "%DEST%\app\google-services.json" "!KEEP!" >nul
  echo google-services.json atual preservado.
)

if exist "%DEST%\app" (
  echo Criando backup da pasta app atual...
  robocopy "%DEST%\app" "%DEST%\_backup_app_antes_v510_real_layout_claro_!TS!" /E >nul
)

echo Aplicando app real com layout novo claro...
robocopy "%SRC%app" "%DEST%\app" /MIR >nul

echo Aplicando workflow e Gradle...
if not exist "%DEST%\.github" mkdir "%DEST%\.github"
robocopy "%SRC%.github" "%DEST%\.github" /E >nul
copy /Y "%SRC%build.gradle.kts" "%DEST%\build.gradle.kts" >nul
copy /Y "%SRC%settings.gradle.kts" "%DEST%\settings.gradle.kts" >nul
copy /Y "%SRC%gradle.properties" "%DEST%\gradle.properties" >nul
copy /Y "%SRC%.gitignore" "%DEST%\.gitignore" >nul

if exist "!KEEP!" (
  copy /Y "!KEEP!" "%DEST%\app\google-services.json" >nul
  echo google-services.json restaurado no app.
)

echo Limpando cache local do projeto para evitar erro de recurso antigo...
if exist "%DEST%\.gradle" rmdir /S /Q "%DEST%\.gradle" 2>nul
if exist "%DEST%\app\build" rmdir /S /Q "%DEST%\app\build" 2>nul
if exist "%DEST%\build" rmdir /S /Q "%DEST%\build" 2>nul

echo Removendo arquivos antigos desnecessarios do topo do repositorio...
del /Q "%DEST%\README.md" 2>nul
del /Q "%DEST%\LEIA*.txt" 2>nul
del /Q "%DEST%\*.md" 2>nul

echo.
echo Aplicado com sucesso.
echo Agora rode dentro do repositorio:
echo.
echo   git status
echo   git add .
echo   git commit -m "Aplicar V5.10 real com layout claro"
echo   git push
echo.
pause
