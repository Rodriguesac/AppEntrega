@echo off
setlocal
cd /d %~dp0

echo.
echo ================================================
echo  Rodrigues Entregador - V2 PainelUP LIMPO
echo ================================================
echo.
echo Limpando restos da versao nativa/Compose que quebram o build...

if exist "app\src\main\java\com\rodriguesacai\entregador\ui\DriverHomeScreen.kt" del /f /q "app\src\main\java\com\rodriguesacai\entregador\ui\DriverHomeScreen.kt"
if exist "app\src\main\java\com\rodriguesacai\entregador\ui\UrgentRideScreen.kt" del /f /q "app\src\main\java\com\rodriguesacai\entregador\ui\UrgentRideScreen.kt"
if exist "app\src\main\java\com\rodriguesacai\entregador\service\OnlineDriverService.kt" del /f /q "app\src\main\java\com\rodriguesacai\entregador\service\OnlineDriverService.kt"
if exist "app\src\main\java\com\rodriguesacai\entregador\data" rmdir /s /q "app\src\main\java\com\rodriguesacai\entregador\data"
if exist "app\src\main\java\com\rodriguesacai\entregador\model" rmdir /s /q "app\src\main\java\com\rodriguesacai\entregador\model"
if exist "app\src\main\java\com\rodriguesacai\entregador\models" rmdir /s /q "app\src\main\java\com\rodriguesacai\entregador\models"
if exist "app\src\main\java\com\rodriguesacai\entregador\repository" rmdir /s /q "app\src\main\java\com\rodriguesacai\entregador\repository"
if exist "app\src\main\java\com\rodriguesacai\entregador\repositories" rmdir /s /q "app\src\main\java\com\rodriguesacai\entregador\repositories"
if exist "app\src\main\java\com\rodriguesacai\entregador\viewmodel" rmdir /s /q "app\src\main\java\com\rodriguesacai\entregador\viewmodel"
if exist "app\src\main\java\com\rodriguesacai\entregador\viewmodels" rmdir /s /q "app\src\main\java\com\rodriguesacai\entregador\viewmodels"

for /f %%i in ('dir /b "app\src\main\java\com\rodriguesacai\entregador\ui" 2^>nul ^| find /c /v ""') do set UI_COUNT=%%i
if "%UI_COUNT%"=="0" rmdir /q "app\src\main\java\com\rodriguesacai\entregador\ui" 2>nul

echo.
echo Status do Git depois da limpeza:
git status

echo.
echo Enviando V2 limpa para o repositorio...
git add -A
git commit -m "V2 PainelUP hibrido limpo"
git push

echo.
echo Finalizado. Agora abra o GitHub Actions e baixe o APK no artifact.
pause
