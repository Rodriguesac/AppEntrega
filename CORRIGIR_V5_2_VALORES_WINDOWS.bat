@echo off
setlocal EnableExtensions

echo.
echo ==========================================
echo  Corrigir V5.2 - valores/repasse
echo ==========================================
echo.

if not exist ".git" (
  echo ERRO: Rode este BAT dentro da pasta do repositorio:
  echo C:\RSITE\rodrigues-entregador-nativo
  pause
  exit /b 1
)

if not exist "app\src\main\java\com\rodriguesacai\entregador\data\DriverRepository.kt" (
  echo ERRO: DriverRepository.kt nao encontrado.
  echo Confira se voce extraiu o ZIP na raiz certa do projeto.
  pause
  exit /b 1
)

echo Arquivo de valores aplicado.
echo Fazendo commit e push...
echo.

git add -A
git commit -m "Corrige valores e repasse V5.2"
git push

echo.
echo Finalizado. Abra o GitHub Actions e gere o APK novamente.
echo.
pause
endlocal
