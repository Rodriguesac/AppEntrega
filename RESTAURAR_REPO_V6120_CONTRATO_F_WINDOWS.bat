@echo off
setlocal EnableExtensions EnableDelayedExpansion
title Restaurar App V6.12.0 Correção F

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "ZIP=%USERPROFILE%\Downloads\rodrigues-entregador-v6-12-0-contrato-f.zip"
set "TMP=C:\TEMP\rodrigues-v6120-contrato-f"
set "BRANCH=main"

if not "%~1"=="" set "ZIP=%~1"

echo.
echo ============================================================
echo  RESTAURAR APP V6.12.0 - CONTRATO F
echo ============================================================
echo.
echo Repo: %REPO%
echo ZIP : %ZIP%
echo.

if not exist "%REPO%\.git" (
  echo ERRO: nao encontrei .git em %REPO%
  pause
  exit /b 1
)

if not exist "%ZIP%" (
  echo ERRO: ZIP nao encontrado.
  echo Coloque em Downloads: rodrigues-entregador-v6-12-0-contrato-f.zip
  pause
  exit /b 1
)

rmdir /s /q "%TMP%" 2>nul
mkdir "%TMP%" 2>nul

powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%TMP%' -Force"
if errorlevel 1 (
  echo ERRO ao extrair ZIP.
  pause
  exit /b 1
)

set "SRC="
for /f "delims=" %%F in ('dir /s /b "%TMP%\settings.gradle.kts" 2^>nul') do (
  set "SRC=%%~dpF"
  goto :FOUND_SRC
)
:FOUND_SRC
if "%SRC%"=="" (
  echo ERRO: nao achei settings.gradle.kts dentro do ZIP.
  pause
  exit /b 1
)
if "%SRC:~-1%"=="\" set "SRC=%SRC:~0,-1%"

cd /d "%REPO%"

powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem -Force '%REPO%' | Where-Object { $_.Name -ne '.git' } | Remove-Item -Recurse -Force"
robocopy "%SRC%" "%REPO%" /E /XD .git
if %ERRORLEVEL% GEQ 8 (
  echo ERRO no robocopy.
  pause
  exit /b 1
)

findstr /i "versionCode versionName" "%REPO%\app\build.gradle.kts"

git add -A
git commit -m "Restaura V6.12.0 contrato Cliente Gestor App"
if errorlevel 1 git commit --allow-empty -m "Forca build V6.12.0 contrato F"

git push origin %BRANCH%
if errorlevel 1 (
  echo ERRO no push.
  pause
  exit /b 1
)

echo.
echo PRONTO. Abra GitHub Actions.
pause