@echo off
setlocal
set "OUT=%USERPROFILE%\Desktop\crash-rodrigues-entregador.txt"
echo.
echo Conecte o celular com depuracao USB ativada.
echo O log sera salvo em:
echo %OUT%
echo.
adb devices
pause
echo Limpando log antigo...
adb logcat -c
echo.
echo Agora abra o app e faca o login ate ele fechar.
echo Quando fechar, volte aqui e aperte CTRL+C para parar.
echo.
adb logcat -v time *:E > "%OUT%"
