@echo off
setlocal enabledelayedexpansion
echo.
echo ===============================================
echo  RODRIGUES ENTREGADOR NATIVO V4 POLIDO
echo ===============================================
echo.
echo Este BAT apenas confere a estrutura. Ele NAO faz commit e NAO faz push.
echo.
if not exist "app\build.gradle.kts" (
  echo ERRO: app\build.gradle.kts nao encontrado.
  pause
  exit /b 1
)
if not exist "app\google-services.json" (
  echo ERRO: app\google-services.json nao encontrado.
  pause
  exit /b 1
)
if not exist ".github\workflows\build-apk.yml" (
  echo ERRO: workflow GitHub Actions nao encontrado.
  pause
  exit /b 1
)
echo Estrutura OK.
echo.
echo Para subir ao Git quando quiser:
echo   git status
echo   git add -A
echo   git commit -m "Rodrigues Entregador nativo V4 polido"
echo   git push
echo.
pause
