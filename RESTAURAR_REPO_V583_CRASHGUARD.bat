@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "BRANCH=main"
set "TMP=C:\TEMP\rodrigues-v583-crashguard"

if "%~1"=="" (
  set "ZIP=%USERPROFILE%\Downloads\rodrigues-entregador-v5-8-3-crashguard.zip"
) else (
  set "ZIP=%~1"
)

echo.
echo ============================================
echo RODRIGUES ENTREGADOR - RESTAURAR V5.8.3
echo CRASHGUARD POS-LOGIN + CARROSSEL DO PAINEL
echo ============================================
echo.

if not exist "%REPO%\.git" (
  echo ERRO: nao encontrei .git em:
  echo %REPO%
  echo.
  pause
  exit /b 1
)

if not exist "%ZIP%" (
  echo ERRO: ZIP nao encontrado:
  echo %ZIP%
  echo.
  echo Baixe o arquivo rodrigues-entregador-v5-8-3-crashguard.zip
  echo e coloque em Downloads, ou arraste o ZIP em cima deste BAT.
  echo.
  pause
  exit /b 1
)

echo Limpando temporario...
rmdir /s /q "%TMP%" 2>nul
mkdir "%TMP%" 2>nul

echo Extraindo ZIP...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%TMP%' -Force"
if errorlevel 1 (
  echo ERRO ao extrair ZIP.
  pause
  exit /b 1
)

set "SRC="
if exist "%TMP%\settings.gradle.kts" set "SRC=%TMP%"
if "%SRC%"=="" (
  for /d %%D in ("%TMP%\*") do (
    if exist "%%D\settings.gradle.kts" set "SRC=%%D"
    if exist "%%D\settings.gradle" set "SRC=%%D"
  )
)

if "%SRC%"=="" (
  echo ERRO: nao achei settings.gradle.kts no ZIP extraido.
  echo Pasta temporaria: %TMP%
  pause
  exit /b 1
)

echo.
echo Origem: %SRC%
echo Destino: %REPO%
echo.

cd /d "%REPO%"

echo Limpando repositorio preservando .git...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem -Force '%REPO%' | Where-Object { $_.Name -ne '.git' } | Remove-Item -Recurse -Force"

echo Copiando projeto novo completo...
robocopy "%SRC%" "%REPO%" /E /XD .git >nul
set "RC=%ERRORLEVEL%"
if %RC% GEQ 8 (
  echo ERRO no ROBOCOPY. Codigo: %RC%
  pause
  exit /b 1
)

cd /d "%REPO%"

echo Conferindo versao declarada:
findstr /C:"versionCode" app\build.gradle.kts
findstr /C:"versionName" app\build.gradle.kts

echo.
echo Enviando para GitHub...
git add -A
git commit -m "Restaura V5.8.3 crashguard pos-login carrossel"
if errorlevel 1 (
  echo.
  echo Nada para commitar ou erro no commit. Vou tentar push mesmo assim.
)

git push origin %BRANCH%
if errorlevel 1 (
  echo ERRO no push. Confira internet/login do GitHub.
  pause
  exit /b 1
)

echo.
echo OK: repositorio restaurado e enviado.
echo Agora abra o GitHub Actions e baixe o APK novo.
echo.
pause
