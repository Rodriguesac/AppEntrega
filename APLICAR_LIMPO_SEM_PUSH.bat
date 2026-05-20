@echo off
setlocal enabledelayedexpansion
echo.
echo ===============================================
echo  RODRIGUES ENTREGADOR NATIVO V6 REAL SEM MOCK
echo ===============================================
echo.
echo Este BAT confere a estrutura. Nao compila no PC, nao faz commit e nao faz push.
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
echo Estrutura OK para GitHub Actions.
echo.
echo Para subir:
echo   git status
echo   git add -A
echo   git commit -m "Rodrigues Entregador nativo V6 real sem mock"
echo   git push
echo.
pause
