@echo off
setlocal
cd /d "%~dp0"
echo Aplicando V3.1 som, alerta e rota...
git add -A
git commit -m "V3.1 som alerta e rota"
git push
echo.
echo Finalizado. Abra o GitHub Actions e baixe o APK quando o build ficar verde.
pause
endlocal
