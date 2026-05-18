@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.6 - UI, tema e fluxo urgente

echo.
echo ==========================================================
echo  RODRIGUES ENTREGADOR V5.6
echo  TEMA CLARO/ESCURO + URGENTE COM DETALHES + EM ANDAMENTO
echo ==========================================================
echo.
echo Rode dentro de:
echo   C:\RSITE\rodrigues-entregador-nativo
echo.
echo Corrige:
echo - login enxuto
echo - tema claro/escuro
echo - olhinho para ocultar valores
echo - alerta urgente abre primeiro com Ver detalhes
echo - aceitar/recusar ficam dentro dos detalhes
echo - corrida aceita permanece na Home como EM ANDAMENTO
echo - card operacional com mapa, coleta, entrega, valor, tempo e distancia
echo.

cd /d "%~dp0"

if not exist "app\src\main\java\com\rodriguesacai\entregador" (
  echo ERRO: execute este BAT na raiz do projeto rodrigues-entregador-nativo.
  pause
  exit /b 1
)

set "BACKUP=_backup_v5_6_ui_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "BACKUP=%BACKUP: =0%"
mkdir "%BACKUP%" >nul 2>nul

echo Movendo arquivos antigos e pacotes ZIP antigos para backup...
for %%F in (APLICAR_E_LIMPAR_*.bat) do (
  if /I not "%%~nxF"=="APLICAR_E_LIMPAR_V5_6_UI_FLUXO_WINDOWS.bat" move "%%F" "%BACKUP%\" >nul 2>nul
)
for %%F in (*.zip) do (
  move "%%F" "%BACKUP%\" >nul 2>nul
)
for /d %%D in (arquivos rodrigues-entregador-nativo-v55-radar-login rodrigues-entregador-nativo-v56-radar-direto-login-pro) do (
  if exist "%%D" move "%%D" "%BACKUP%\" >nul 2>nul
)

echo Limpando build/cache...
if exist ".gradle" rmdir /s /q ".gradle"
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"

echo.
echo Enviando V5.6 para o GitHub...
git add -A
git commit -m "V5.6 UI tema claro escuro fluxo urgente em andamento"
git push

echo.
echo Finalizado. Rode o GitHub Actions e baixe o APK.
pause
