@echo off
setlocal enabledelayedexpansion
chcp 65001 >nul

set "DEST=C:\RSITE\rodrigues-entregador-nativo"
set "SRC=%~dp0"
set "MSG=Aplicar V5.10 real com layout claro"

echo.
echo ================================================================
echo  RODRIGUES ENTREGADOR - APLICAR + COMMIT + PUSH AUTOMATICO
echo ================================================================
echo.
echo Destino fixo:
echo   %DEST%
echo.
echo Vai aplicar o app real/Firebase mantendo o layout NOVO em tema claro.
echo Vai preservar app\google-services.json se existir.
echo Vai fazer git add, commit e push no final.
echo.

if not exist "%DEST%" (
  echo ERRO: Pasta nao encontrada:
  echo   %DEST%
  pause
  exit /b 1
)

where git >nul 2>nul
if errorlevel 1 (
  echo ERRO: Git nao encontrado no Windows.
  echo Instale o Git ou abra pelo Git Bash/CMD onde o git funciona.
  pause
  exit /b 1
)

cd /d "%DEST%"
if not exist ".git" (
  echo ERRO: Esta pasta nao parece ser um repositorio Git:
  echo   %DEST%
  pause
  exit /b 1
)

for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd_HHmmss"') do set TS=%%i
set "KEEP=%TEMP%\google-services-v510-!TS!.json"

if exist "%DEST%\app\google-services.json" (
  copy /Y "%DEST%\app\google-services.json" "!KEEP!" >nul
  echo OK: google-services.json atual preservado.
) else (
  echo AVISO: Nao encontrei %DEST%\app\google-services.json
  echo O app pode compilar, mas Firebase real pode nao conectar sem esse arquivo.
)

if exist "%DEST%\app" (
  echo Criando backup da pasta app atual...
  robocopy "%DEST%\app" "%DEST%\_backup_app_antes_v510_auto_!TS!" /E /NFL /NDL /NJH /NJS /NC /NS >nul
)

echo Aplicando app real com layout novo claro...
robocopy "%SRC%app" "%DEST%\app" /MIR /NFL /NDL /NJH /NJS /NC /NS >nul
if errorlevel 8 (
  echo ERRO: Falha ao copiar a pasta app.
  pause
  exit /b 1
)

echo Aplicando workflow e arquivos Gradle...
if not exist "%DEST%\.github" mkdir "%DEST%\.github"
robocopy "%SRC%.github" "%DEST%\.github" /E /NFL /NDL /NJH /NJS /NC /NS >nul
copy /Y "%SRC%build.gradle.kts" "%DEST%\build.gradle.kts" >nul
copy /Y "%SRC%settings.gradle.kts" "%DEST%\settings.gradle.kts" >nul
copy /Y "%SRC%gradle.properties" "%DEST%\gradle.properties" >nul
copy /Y "%SRC%.gitignore" "%DEST%\.gitignore" >nul

if exist "!KEEP!" (
  copy /Y "!KEEP!" "%DEST%\app\google-services.json" >nul
  echo OK: google-services.json restaurado no app.
)

echo Limpando caches para evitar reaproveitar recurso antigo quebrado...
if exist "%DEST%\.gradle" rmdir /S /Q "%DEST%\.gradle" 2>nul
if exist "%DEST%\app\build" rmdir /S /Q "%DEST%\app\build" 2>nul
if exist "%DEST%\build" rmdir /S /Q "%DEST%\build" 2>nul

echo Removendo arquivos antigos inuteis do topo do repositorio...
del /Q "%DEST%\README.md" 2>nul
del /Q "%DEST%\LEIA*.txt" 2>nul
del /Q "%DEST%\*.md" 2>nul

echo.
echo Conferindo alteracoes...
git status --short

echo.
echo Fazendo commit e push...
git add .

git diff --cached --quiet
if errorlevel 1 (
  git commit -m "%MSG%"
  if errorlevel 1 (
    echo ERRO: Falha no commit.
    pause
    exit /b 1
  )
) else (
  echo Nada novo para commitar. Tentando push mesmo assim...
)

git push
if errorlevel 1 (
  echo.
  echo ERRO: O push falhou.
  echo Se pedir login/token, abra o GitHub Desktop ou Git Bash e autentique sua conta.
  echo Depois rode novamente este BAT.
  pause
  exit /b 1
)

echo.
echo ================================================================
echo  PRONTO: aplicado, commitado e enviado para o GitHub.
echo  Agora acompanhe a Action para baixar o APK.
echo ================================================================
pause
