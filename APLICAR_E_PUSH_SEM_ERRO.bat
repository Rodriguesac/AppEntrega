@echo off
set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "SRC=%~dp0"
set "APP_SRC=%SRC%app"
set "APP_DST=%REPO%\app"
set "TMP_GS=%TEMP%\google-services-rodrigues-entregador.json"
set "DRIVER=%APP_DST%\src\main\java\com\rodriguesacai\entregador\ui\DriverHomeScreen.kt"

echo.
echo ============================================================
echo UP ENTREGAS RODRIGUES - V5.11 LAYOUT CLARO REAL
echo ============================================================
echo Destino: %REPO%
echo.

if not exist "%REPO%\" (
  echo ERRO: Pasta do repositorio nao existe.
  echo %REPO%
  pause
  exit /b 1
)

if not exist "%REPO%\.git\" (
  echo ERRO: A pasta existe, mas nao tem .git.
  echo %REPO%
  pause
  exit /b 1
)

if not exist "%APP_SRC%\" (
  echo ERRO: A pasta app do pacote nao foi encontrada.
  echo %APP_SRC%
  pause
  exit /b 1
)

cd /d "%REPO%"
if errorlevel 1 (
  echo ERRO: Nao consegui entrar no repositorio.
  pause
  exit /b 1
)

echo [1/8] Salvando google-services.json atual...
if exist "%APP_DST%\google-services.json" (
  copy /Y "%APP_DST%\google-services.json" "%TMP_GS%" >nul
  echo OK: google-services.json salvo.
) else (
  echo AVISO: google-services.json nao existe no app atual.
)

echo.
echo [2/8] Limpando app antigo e caches...
if exist "%APP_DST%\build\" rmdir /S /Q "%APP_DST%\build"
if exist "%REPO%\build\" rmdir /S /Q "%REPO%\build"
if exist "%REPO%\.gradle\" rmdir /S /Q "%REPO%\.gradle"
if exist "%APP_DST%\src\" rmdir /S /Q "%APP_DST%\src"
if exist "%APP_DST%\build.gradle.kts" del /F /Q "%APP_DST%\build.gradle.kts"
if exist "%APP_DST%\build.gradle" del /F /Q "%APP_DST%\build.gradle"
if not exist "%APP_DST%\" mkdir "%APP_DST%"

echo.
echo [3/8] Copiando app novo...
robocopy "%APP_SRC%" "%APP_DST%" /E /XF google-services.json /XD build .gradle
if errorlevel 8 (
  echo ERRO: Falha no robocopy.
  pause
  exit /b 1
)

echo.
echo [4/8] Copiando arquivos Gradle e workflow...
copy /Y "%SRC%build.gradle.kts" "%REPO%\build.gradle.kts" >nul
copy /Y "%SRC%settings.gradle.kts" "%REPO%\settings.gradle.kts" >nul
copy /Y "%SRC%gradle.properties" "%REPO%\gradle.properties" >nul
if not exist "%REPO%\.github\workflows\" mkdir "%REPO%\.github\workflows"
copy /Y "%SRC%.github\workflows\build-apk.yml" "%REPO%\.github\workflows\build-apk.yml" >nul
if exist "%SRC%.gitignore" copy /Y "%SRC%.gitignore" "%REPO%\.gitignore" >nul

echo.
echo [5/8] Restaurando google-services.json...
if exist "%TMP_GS%" (
  copy /Y "%TMP_GS%" "%APP_DST%\google-services.json" >nul
  echo OK: google-services.json restaurado.
) else (
  echo AVISO: Sem google-services.json para restaurar.
)

echo.
echo [6/8] Validando arquivo principal...
if not exist "%DRIVER%" (
  echo ERRO: DriverHomeScreen.kt nao encontrado.
  echo %DRIVER%
  pause
  exit /b 1
)

findstr /C:"import androidx.compose.foundation.layout.Column" "%DRIVER%" >nul
if errorlevel 1 (
  echo ERRO: Import Column nao encontrado. Pacote incorreto.
  pause
  exit /b 1
)

findstr /C:"import androidx.compose.material3.NavigationBarItem" "%DRIVER%" >nul
if errorlevel 1 (
  echo ERRO: Import NavigationBarItem nao encontrado. Pacote incorreto.
  pause
  exit /b 1
)

findstr /C:"import androidx.compose.material3.NavigationBarItemDefaults" "%DRIVER%" >nul
if errorlevel 1 (
  echo ERRO: Import NavigationBarItemDefaults nao encontrado. Pacote incorreto.
  pause
  exit /b 1
)

echo OK: imports Compose validados.

echo.
echo [7/8] Enviando para GitHub...
git status --short
git add -A
git commit -m "Aplicar V5.11 layout claro real corrigido"
if errorlevel 1 (
  echo AVISO: Nada novo para commit ou commit ja existe.
)

git push
if errorlevel 1 (
  echo.
  echo ERRO: git push falhou.
  echo Se pedir login, autentique no GitHub Desktop ou Git Bash e rode de novo.
  pause
  exit /b 1
)

echo.
echo [8/8] PRONTO.
echo Agora abra GitHub Actions e veja o build NOVO no topo da lista.
echo Nao olhe o build antigo #59.
echo.
pause
exit /b 0
