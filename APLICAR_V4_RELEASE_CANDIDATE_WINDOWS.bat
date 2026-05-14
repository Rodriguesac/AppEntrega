@echo off
setlocal EnableExtensions

echo Aplicando V4 Release Candidate 100%% nativo...

if not exist ".git" (
  echo ERRO: rode este arquivo dentro da raiz do projeto Git.
  pause
  exit /b 1
)

if exist "app\src\main\assets" rmdir /S /Q "app\src\main\assets"
if exist "app\src\main\java\com\rodriguesacai\entregador\web" rmdir /S /Q "app\src\main\java\com\rodriguesacai\entregador\web"
if exist "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt" del /Q "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt"

if exist ".gradle" rmdir /S /Q ".gradle"
if exist "build" rmdir /S /Q "build"
if exist "app\build" rmdir /S /Q "app\build"

git add -A
git commit -m "V4 release candidate nativo"
git push

echo.
echo Pronto. Abra o GitHub Actions e baixe o APK se o build ficar verde.
pause
endlocal
