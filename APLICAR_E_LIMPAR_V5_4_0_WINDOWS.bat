@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.4.0 - Torre V9.4

echo.
echo ==========================================================
echo  RODRIGUES ENTREGADOR V5.4.0
echo  COMPATIVEL COM TORRE V9.4 OPERACIONAL
echo ==========================================================
echo.
echo Este pacote ajusta o app para receber oferta real da Torre:
echo - rotas_entrega e pedidos
echo - status BUSCANDO_ENTREGADOR / OFERTA
echo - liberadoParaEntregador / ofertaLiberada
echo - targetDriverId / entregadorAtualOferta
echo.
echo Tambem melhora o listener para nao depender dos 120 primeiros docs.
echo.

cd /d "%~dp0"

if not exist "app" (
  echo ERRO: execute este BAT na raiz do projeto rodrigues-entregador-nativo.
  pause
  exit /b 1
)

set "BACKUP=_backup_antigos_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "BACKUP=%BACKUP: =0%"
mkdir "%BACKUP%" >nul 2>nul

echo Movendo arquivos antigos para %BACKUP% ...

for %%F in (*.bat README*.md README*.txt LEIA*.txt) do (
  if /I not "%%~nxF"=="APLICAR_E_LIMPAR_V5_4_0_WINDOWS.bat" (
    move "%%F" "%BACKUP%\" >nul 2>nul
  )
)

if exist "docs" move "docs" "%BACKUP%\docs" >nul 2>nul

for /d %%D in (_backup*) do (
  if /I not "%%~nxD"=="%BACKUP%" (
    move "%%D" "%BACKUP%\%%~nxD" >nul 2>nul
  )
)

echo Limpando cache/build local...
if exist ".gradle" rmdir /s /q ".gradle"
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"

echo.
echo Enviando para o GitHub...
git add -A
git commit -m "V5.4 app compativel com Torre V9.4"
git push

echo.
echo Finalizado. Agora rode o GitHub Actions para gerar o APK.
pause
