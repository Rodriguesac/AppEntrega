@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.5.0 - Radar Fix + Login

echo.
echo ==========================================================
echo  RODRIGUES ENTREGADOR V5.5.0
echo  CORRECAO DO RADAR + LOGIN NOVO
echo ==========================================================
echo.
echo Ajustes deste pacote:
echo - Corrige oferta de pedido individual que nao tocava no app.
echo - Mantem o listener do radar ativo quando o entregador estava online.
echo - Cria novo canal de notificacao urgente para som/tela cheia.
echo - Melhora visual da tela de login/cadastro.
echo.

cd /d "%~dp0"

if not exist "app" (
  echo ERRO: execute este BAT na raiz do projeto rodrigues-entregador-nativo.
  pause
  exit /b 1
)

echo Limpando arquivos antigos inutiles...
for %%F in (README*.md README*.txt LEIA*.txt APLICAR_E_LIMPAR_V5_4_0_WINDOWS.bat) do (
  if exist "%%F" del /f /q "%%F" >nul 2>nul
)
if exist "docs" rmdir /s /q "docs" >nul 2>nul
for /d %%D in (_backup*) do rmdir /s /q "%%D" >nul 2>nul

echo Limpando cache/build local...
if exist ".gradle" rmdir /s /q ".gradle" >nul 2>nul
if exist "build" rmdir /s /q "build" >nul 2>nul
if exist "app\build" rmdir /s /q "app\build" >nul 2>nul

echo.
echo Enviando para o GitHub...
git add -A
git commit -m "V5.5 corrige radar e melhora login"
git push

echo.
echo Finalizado. Agora rode o GitHub Actions para gerar o APK.
pause
