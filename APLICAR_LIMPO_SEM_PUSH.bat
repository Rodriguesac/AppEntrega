@echo off
setlocal enabledelayedexpansion
echo.
echo ============================================
echo  RODRIGUES ENTREGADOR NATIVO V2 - SEM PUSH
echo ============================================
echo.
echo Rode este BAT dentro de C:\RSITE\rodrigues-entregador-nativo depois de extrair o ZIP por cima.
echo Ele NAO faz commit e NAO faz push.
echo.
if not exist "app\build.gradle.kts" (
  echo ERRO: app\build.gradle.kts nao encontrado. Confira se voce esta na raiz do projeto.
  pause
  exit /b 1
)
if not exist ".github\workflows\build-apk.yml" (
  echo ERRO: workflow nao encontrado.
  pause
  exit /b 1
)
if not exist "app\google-services.json" (
  echo ERRO: google-services.json nao encontrado.
  pause
  exit /b 1
)
echo Estrutura pronta para GitHub Actions.
echo.
echo Proximo passo, quando for aprovado:
echo   git status
echo   git add .
echo   git commit -m "Rodrigues Entregador nativo V2"
echo   git push

echo.
pause
