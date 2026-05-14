@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ========================================
echo Rodrigues Entregador V3 - 100%% NATIVO
echo ========================================
echo.

echo Limpando sobras da versão híbrida/WebView...
if exist "app\src\main\assets" rmdir /s /q "app\src\main\assets"
if exist "app\src\main\java\com\rodriguesacai\entregador\web" rmdir /s /q "app\src\main\java\com\rodriguesacai\entregador\web"
if exist "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt" del /f /q "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt"

echo Conferindo status...
git status

echo Enviando alterações...
git add -A
git commit -m "V3 100 porcento nativo"
git push

echo.
echo Finalizado. Agora abra o GitHub Actions e baixe o APK quando o build ficar verde.
pause
