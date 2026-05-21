@echo off
setlocal
cd /d "%~dp0"
echo Limpando workflows antigos para deixar apenas android-debug.yml...
if exist ".github\workflows" (
  for %%F in (.github\workflows\*.yml) do (
    if /I not "%%~nxF"=="android-debug.yml" del /f /q "%%F"
  )
  for %%F in (.github\workflows\*.yaml) do del /f /q "%%F"
)
echo Limpando APKs/zips antigos soltos que confundem download...
for %%F in (*debug-apk*.zip *fonte*.zip *icones*.zip *v8*.zip *v9*.zip *v10*.zip *v11*.zip *v15*.zip) do del /f /q "%%F" 2>nul
echo Conferindo versao do app:
findstr /i "versionCode versionName" app\build.gradle.kts
echo.
echo Agora rode:
echo git status
echo git add -A
echo git commit -m "Rodrigues Entregador V16 APK unico"
echo git push origin main
pause
