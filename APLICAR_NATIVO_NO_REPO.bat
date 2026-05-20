@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "REPO=C:\RSITE"
set "SRC=%~dp0"
set "BACKUP=%TEMP%\google-services-rodrigues-backup.json"

echo.
echo ============================================
echo  RODRIGUES ENTREGADOR - APLICAR NATIVO
echo ============================================
echo.
echo Este BAT NAO faz git add, NAO faz commit e NAO faz push.
echo Ele apenas substitui os arquivos do projeto pela base nativa limpa.
echo.

if not exist "%REPO%" (
  echo ERRO: pasta %REPO% nao existe.
  pause
  exit /b 1
)

if exist "%REPO%\app\google-services.json" (
  copy /Y "%REPO%\app\google-services.json" "%BACKUP%" >nul
)

robocopy "%SRC%" "%REPO%" /MIR /XD ".git" ".gradle" "build" "app\build" /XF "APLICAR_NATIVO_NO_REPO.bat" >nul

if exist "%BACKUP%" (
  if not exist "%REPO%\app" mkdir "%REPO%\app"
  copy /Y "%BACKUP%" "%REPO%\app\google-services.json" >nul
)

echo.
echo Aplicado em %REPO%.
echo Agora revise localmente. Nao foi enviado nada para o Git.
echo.
pause
