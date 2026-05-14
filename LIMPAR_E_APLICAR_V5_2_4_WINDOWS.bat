@echo off
chcp 65001 >nul
title Limpar Rodrigues Entregador e aplicar V5.2.4

echo.
echo ==========================================================
echo  RODRIGUES ENTREGADOR - LIMPEZA SEGURA + APLICAR V5.2.4
echo ==========================================================
echo.
echo Este BAT NAO apaga app, .github, build.gradle, settings.gradle
echo nem arquivos principais do projeto.
echo.
echo Ele move arquivos antigos de ajuda/README/BATs velhos/docs para backup
echo e depois executa o BAT final:
echo APLICAR_V5_2_4_MAPA_LIMPO_WINDOWS.bat
echo.

cd /d "%~dp0"

if not exist "app" (
  echo ERRO: Esta pasta nao parece ser a raiz do projeto.
  echo Abra a pasta C:\RSITE\rodrigues-entregador-nativo e execute de la.
  pause
  exit /b 1
)

set "BACKUP=_backup_limpeza_%date:~-4%%date:~3,2%%date:~0,2%_%time:~0,2%%time:~3,2%%time:~6,2%"
set "BACKUP=%BACKUP: =0%"

mkdir "%BACKUP%" >nul 2>nul

echo.
echo Criando backup em: %BACKUP%
echo.

echo Movendo READMEs antigos...
for %%F in (README*.txt LEIA*.txt) do (
  if exist "%%F" move "%%F" "%BACKUP%\" >nul
)

echo Movendo BATs antigos, mantendo apenas o V5.2.4...
for %%F in (*.bat) do (
  if /I not "%%~nxF"=="APLICAR_V5_2_4_MAPA_LIMPO_WINDOWS.bat" (
    if /I not "%%~nxF"=="LIMPAR_E_APLICAR_V5_2_4_WINDOWS.bat" (
      move "%%F" "%BACKUP%\" >nul
    )
  )
)

echo Movendo pasta docs antiga, se existir...
if exist "docs" move "docs" "%BACKUP%\docs" >nul

echo Limpando lixos temporarios comuns...
if exist ".gradle" rmdir /s /q ".gradle"
if exist "build" rmdir /s /q "build"
if exist "app\build" rmdir /s /q "app\build"

echo.
echo ==========================================================
echo  LIMPEZA CONCLUIDA
echo ==========================================================
echo.
echo Foram mantidos:
echo - app
echo - .github
echo - build.gradle.kts / settings.gradle.kts / gradle.properties
echo - APLICAR_V5_2_4_MAPA_LIMPO_WINDOWS.bat
echo.
echo Os arquivos antigos foram movidos para:
echo %BACKUP%
echo.

if exist "APLICAR_V5_2_4_MAPA_LIMPO_WINDOWS.bat" (
  echo Agora vou executar o BAT final da V5.2.4...
  echo.
  call "APLICAR_V5_2_4_MAPA_LIMPO_WINDOWS.bat"
) else (
  echo ERRO: Nao encontrei APLICAR_V5_2_4_MAPA_LIMPO_WINDOWS.bat
  echo Extraia/copiei o ultimo ZIP V5.2.4 para esta pasta e rode novamente.
  pause
  exit /b 1
)

pause
