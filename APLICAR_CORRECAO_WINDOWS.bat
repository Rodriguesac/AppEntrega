@echo off
chcp 65001 >nul
cd /d "%~dp0"
echo Limpando pacote antigo que estava quebrando o build...
if exist "app\src\main\java\com\rodrigues\entregador" rmdir /s /q "app\src\main\java\com\rodrigues\entregador"
if exist "app\src\main\java\com\rodrigues" rmdir /s /q "app\src\main\java\com\rodrigues"
echo OK. Agora faça commit/push no GitHub.
pause
