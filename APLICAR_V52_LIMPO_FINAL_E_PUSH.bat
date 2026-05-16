@echo off
chcp 65001 >nul
title Rodrigues Entregador - Aplicar V5.2 Limpo Final e Push

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "SRC=%~dp0arquivos"
set "BACKUP_GOOGLE=%TEMP%\google-services-rodrigues-backup.json"

echo.
echo ============================================
echo  RODRIGUES ENTREGADOR - V5.2 LIMPO FINAL
echo ============================================
echo.

if not exist "%REPO%\.git" (
    echo ERRO: Repositorio nao encontrado:
    echo %REPO%
    pause
    exit /b 1
)

if not exist "%SRC%\app\build.gradle.kts" (
    echo ERRO: Pasta arquivos nao encontrada.
    echo Extraia o ZIP inteiro antes de executar este BAT.
    echo Esperado:
    echo %SRC%
    pause
    exit /b 1
)

cd /d "%REPO%"

echo Atualizando do GitHub...
git pull --rebase origin main

echo.
echo Backup do google-services.json...
if exist "%REPO%\app\google-services.json" (
    copy /Y "%REPO%\app\google-services.json" "%BACKUP_GOOGLE%" >nul
) else (
    echo ATENCAO: google-services.json nao encontrado em app\google-services.json
    echo Se o build reclamar do Firebase, coloque o arquivo correto depois.
)

echo.
echo Limpando codigo antigo que estava misturado...
if exist "%REPO%\app\src\main\java" rmdir /s /q "%REPO%\app\src\main\java"
if exist "%REPO%\app\src\main\kotlin" rmdir /s /q "%REPO%\app\src\main\kotlin"
if exist "%REPO%\app\src\main\res" rmdir /s /q "%REPO%\app\src\main\res"
if exist "%REPO%\.github\workflows" rmdir /s /q "%REPO%\.github\workflows"

del /q "%REPO%\README.md" 2>nul
del /q "%REPO%\LEIA*.txt" 2>nul
del /q "%REPO%\*.backup*" 2>nul
del /q "%REPO%\app\build.gradle.kts.backup-*" 2>nul
del /q "%REPO%\TorreDeComando.bat" 2>nul

echo.
echo Copiando V5.2 limpo final...
robocopy "%SRC%" "%REPO%" /E /XD ".git" ".gradle" "build" "app\build" >nul

echo.
echo Restaurando google-services.json...
if exist "%BACKUP_GOOGLE%" (
    if not exist "%REPO%\app" mkdir "%REPO%\app"
    copy /Y "%BACKUP_GOOGLE%" "%REPO%\app\google-services.json" >nul
)

echo.
echo Limpando caches locais...
if exist "%REPO%\.gradle" rmdir /s /q "%REPO%\.gradle"
if exist "%REPO%\app\build" rmdir /s /q "%REPO%\app\build"
if exist "%REPO%\build" rmdir /s /q "%REPO%\build"

echo.
echo Conferindo arquivos Kotlin finais:
dir /s /b "%REPO%\app\src\main\java\*.kt"

echo.
echo Conferindo referencias antigas. Se aparecer algo abaixo, me mande print:
findstr /S /N /I "UrgentRideScreen RodriguesNativeTheme RealDeliveryMap RodriguesFonts googlefonts osmdroid coil OnlineDriverService RideFirebaseMessagingService" "%REPO%\app\src\main\java\*.kt"

echo.
echo Status Git:
git status --short

echo.
echo Commitando substituicao limpa...
git add -A
git commit -m "Substitui por V5.2 limpo final"

echo.
echo Enviando para GitHub...
git pull --rebase origin main
git push origin main

if errorlevel 1 (
    echo.
    echo PUSH falhou. Me mande esta tela.
    pause
    exit /b 1
)

echo.
echo ============================================
echo  PRONTO
echo ============================================
echo.
echo Agora rode no GitHub Actions:
echo BUILD APK V5.2 LIMPO FINAL
echo.
pause
