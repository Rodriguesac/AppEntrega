@echo off
setlocal EnableExtensions EnableDelayedExpansion

title Restaurar Rodrigues Entregador V6.5.0 Reconstrucao Visual

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "ZIP_PADRAO=%USERPROFILE%\Downloads\rodrigues-entregador-v6-5-0-reconstrucao-visual.zip"
set "TMP=C:\TEMP\rodrigues-v650-reconstrucao-visual"
set "BRANCH=main"

echo.
echo ============================================================
echo  RESTAURAR REPOSITORIO - V6.5.0 RECONSTRUCAO VISUAL
echo ============================================================
echo.

if not "%~1"=="" (
  set "ZIP=%~1"
) else (
  set "ZIP=%ZIP_PADRAO%"
)

echo Repo: %REPO%
echo ZIP : %ZIP%
echo.

if not exist "%REPO%\.git" (
  echo ERRO: Nao achei a pasta .git em:
  echo %REPO%
  echo.
  echo Confira se o repositorio esta nessa pasta.
  pause
  exit /b 1
)

if not exist "%ZIP%" (
  echo ERRO: ZIP nao encontrado.
  echo.
  echo Coloque este arquivo na pasta Downloads:
  echo rodrigues-entregador-v6-5-0-reconstrucao-visual.zip
  echo.
  echo Ou arraste o ZIP em cima deste BAT.
  pause
  exit /b 1
)

echo Limpando pasta temporaria...
rmdir /s /q "%TMP%" 2>nul
mkdir "%TMP%" 2>nul

echo.
echo Extraindo ZIP...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -LiteralPath '%ZIP%' -DestinationPath '%TMP%' -Force"
if errorlevel 1 (
  echo ERRO: falhou ao extrair o ZIP.
  pause
  exit /b 1
)

echo.
echo Procurando projeto extraido...
set "SRC="

for /f "delims=" %%F in ('dir /s /b "%TMP%\settings.gradle.kts" 2^>nul') do (
  set "SRC=%%~dpF"
  goto :FOUND_SRC
)

for /f "delims=" %%F in ('dir /s /b "%TMP%\settings.gradle" 2^>nul') do (
  set "SRC=%%~dpF"
  goto :FOUND_SRC
)

:FOUND_SRC
if "%SRC%"=="" (
  echo ERRO: nao achei settings.gradle ou settings.gradle.kts dentro do ZIP.
  echo Verifique se baixou o ZIP correto.
  pause
  exit /b 1
)

if "%SRC:~-1%"=="\" set "SRC=%SRC:~0,-1%"

echo Projeto encontrado:
echo %SRC%
echo.

cd /d "%REPO%"

echo Criando branch backup local antes da troca...
git branch backup-antes-v650-reconstrucao-visual 2>nul

echo.
echo Limpando arquivos do repositorio, preservando .git...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem -Force '%REPO%' | Where-Object { $_.Name -ne '.git' } | Remove-Item -Recurse -Force"
if errorlevel 1 (
  echo ERRO: falhou ao limpar o repositorio.
  pause
  exit /b 1
)

echo.
echo Copiando projeto novo...
robocopy "%SRC%" "%REPO%" /E /XD .git
set "ROBO=%ERRORLEVEL%"
if %ROBO% GEQ 8 (
  echo ERRO: robocopy falhou com codigo %ROBO%.
  pause
  exit /b 1
)

echo.
echo Conferindo workflow do GitHub Actions...
if not exist "%REPO%\.github\workflows\build-apk.yml" (
  echo ERRO: workflow nao foi encontrado:
  echo %REPO%\.github\workflows\build-apk.yml
  echo Sem esse arquivo o Actions nao roda.
  pause
  exit /b 1
)

echo.
echo Conferindo versao declarada:
findstr /C:"versionCode" "%REPO%\app\build.gradle.kts"
findstr /C:"versionName" "%REPO%\app\build.gradle.kts"

echo.
echo Adicionando alteracoes...
cd /d "%REPO%"
git add -A

echo.
echo Criando commit...
git commit -m "Rodrigues Entregador V6.5.0 reconstrucao visual fiel"
if errorlevel 1 (
  echo.
  echo Aviso: nada novo para commitar ou commit nao criado.
)

echo.
echo Enviando para GitHub main...
git push origin %BRANCH%
if errorlevel 1 (
  echo.
  echo ERRO: push falhou.
  echo Confira login do GitHub/Git Credential Manager.
  pause
  exit /b 1
)

echo.
echo ============================================================
echo  FINALIZADO - ABRA O GITHUB ACTIONS
echo ============================================================
echo.
echo Artifact esperado:
echo Rodrigues-Entregador-V6-5-0-reconstrucao-visual-debug-apk
echo.
git status
pause
