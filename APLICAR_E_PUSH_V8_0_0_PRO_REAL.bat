@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

title Rodrigues Entregador V8.0.0 Pro Real

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "SRC=%~dp0"
set "BRANCH=main"
set "TMP=%TEMP%\rodrigues-entregador-v800-preservar"

echo.
echo ============================================================
echo  RODRIGUES ENTREGADOR - V8.0.0 PRO REAL
echo  Visual redesenhado + Firebase/Firestore real
echo ============================================================
echo.
echo Origem: %SRC%
echo Destino: %REPO%
echo.

if not exist "%SRC%settings.gradle.kts" (
  echo ERRO: execute este BAT dentro da pasta extraida do pacote V8.0.0.
  pause
  exit /b 1
)

if not exist "%REPO%\.git" (
  echo ERRO: nao achei o repositorio Git em:
  echo %REPO%
  pause
  exit /b 1
)

if exist "%TMP%" rmdir /s /q "%TMP%" >nul 2>nul
mkdir "%TMP%" >nul 2>nul

if exist "%REPO%\app\google-services.json" (
  echo Preservando app\google-services.json para manter conexao Firebase...
  copy /y "%REPO%\app\google-services.json" "%TMP%\google-services.json" >nul
)

cd /d "%REPO%" || exit /b 1

echo Criando branch de backup local antes da substituicao...
for /f "tokens=1-3 delims=/ " %%a in ("%date%") do set "DATA=%%c%%b%%a"
for /f "tokens=1-2 delims=: " %%a in ("%time%") do set "HORA=%%a%%b"
set "BACKUP_BRANCH=backup-antes-v800-pro-real-%DATA%-%HORA%"
set "BACKUP_BRANCH=%BACKUP_BRANCH: =0%"
git branch "%BACKUP_BRANCH%" >nul 2>nul

echo Limpando repositorio, preservando .git...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem -Force '%REPO%' | Where-Object { $_.Name -ne '.git' } | Remove-Item -Recurse -Force"
if errorlevel 1 (
  echo ERRO ao limpar o repositorio.
  pause
  exit /b 1
)

echo Copiando pacote atualizado...
robocopy "%SRC%" "%REPO%" /E /XD .git .gradle build app\build /XF *.zip *.apk *.aab >nul
set "RC=%ERRORLEVEL%"
if %RC% GEQ 8 (
  echo ERRO no ROBOCOPY. Codigo: %RC%
  pause
  exit /b 1
)

if exist "%TMP%\google-services.json" (
  echo Restaurando app\google-services.json...
  if not exist "%REPO%\app" mkdir "%REPO%\app"
  copy /y "%TMP%\google-services.json" "%REPO%\app\google-services.json" >nul
) else (
  echo ATENCAO: nao encontrei google-services.json antigo.
  echo O app compila, mas o Firebase so conecta quando voce colocar app\google-services.json.
)

cd /d "%REPO%" || exit /b 1

echo Limpando arquivos antigos inuteis, se existirem...
for %%F in (README.md README.txt LEIA.txt LEIA-IMPORTANTE.txt LEIA_*.txt README_*.txt README_*.md) do if exist "%%F" del /f /q "%%F" >nul 2>nul
for /d %%D in (_backup* docs arquivos backups antigo antiga) do if exist "%%D" rmdir /s /q "%%D" >nul 2>nul

echo.
echo Conferindo versao:
findstr /C:"versionCode" app\build.gradle.kts
findstr /C:"versionName" app\build.gradle.kts

echo.
echo Status do Git:
git status --short

echo.
echo Commit e push...
git add -A
git commit -m "V8.0.0 app entregador pro real conectado Firebase"
if errorlevel 1 (
  echo Nada novo para commitar ou erro no commit. Tentando push mesmo assim.
)
git push origin %BRANCH%
if errorlevel 1 (
  echo ERRO no push. Verifique login/branch. O projeto foi aplicado localmente.
  pause
  exit /b 1
)

echo.
echo PRONTO. Aguarde o GitHub Actions gerar o artifact:
echo Rodrigues-Entregador-V8-0-0-Pro-Real-debug-apk
echo.
pause
