@echo off
chcp 65001 >nul
setlocal EnableExtensions EnableDelayedExpansion

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "SRC=%~dp0"
set "APP_SRC=%SRC%app"
set "APP_DST=%REPO%\app"
set "GOOGLE_TMP=%TEMP%\google-services-rodrigues-entregador.json"
set "DRIVER=%APP_DST%\src\main\java\com\rodriguesacai\entregador\ui\DriverHomeScreen.kt"

echo.
echo ============================================================
echo   UP ENTREGAS / RODRIGUES - V5.11 LAYOUT CLARO + LOGICA REAL
echo ============================================================
echo Destino: %REPO%
echo.

if not exist "%REPO%" (
  echo ERRO: Pasta do repositorio nao existe: %REPO%
  pause
  exit /b 1
)

if not exist "%REPO%\.git" (
  echo ERRO: A pasta existe, mas nao parece ser repositorio Git: %REPO%
  pause
  exit /b 1
)

if not exist "%APP_SRC%" (
  echo ERRO: Pasta app do pacote nao encontrada: %APP_SRC%
  pause
  exit /b 1
)

cd /d "%REPO%" || exit /b 1

echo [1/9] Salvando google-services.json existente, se houver...
if exist "%APP_DST%\google-services.json" (
  copy /Y "%APP_DST%\google-services.json" "%GOOGLE_TMP%" >nul
  echo OK: google-services.json preservado temporariamente.
) else (
  echo AVISO: Nao encontrei app\google-services.json no repositorio.
)

echo.
echo [2/9] Limpando arquivos antigos que podem manter layout/erros velhos...
if exist "%APP_DST%\build" rmdir /S /Q "%APP_DST%\build"
if exist "%REPO%\build" rmdir /S /Q "%REPO%\build"
if exist "%REPO%\.gradle" rmdir /S /Q "%REPO%\.gradle"
if exist "%APP_DST%\src" rmdir /S /Q "%APP_DST%\src"
if exist "%APP_DST%\build.gradle.kts" del /F /Q "%APP_DST%\build.gradle.kts"
if exist "%APP_DST%\build.gradle" del /F /Q "%APP_DST%\build.gradle"
if not exist "%APP_DST%" mkdir "%APP_DST%"

echo.
echo [3/9] Aplicando app novo em tema claro e mantendo logica real...
robocopy "%APP_SRC%" "%APP_DST%" /E /XF google-services.json /XD build .gradle >nul
if errorlevel 8 (
  echo ERRO: Falha ao copiar app com robocopy.
  pause
  exit /b 1
)

copy /Y "%SRC%build.gradle.kts" "%REPO%\build.gradle.kts" >nul
copy /Y "%SRC%settings.gradle.kts" "%REPO%\settings.gradle.kts" >nul
copy /Y "%SRC%gradle.properties" "%REPO%\gradle.properties" >nul
if not exist "%REPO%\.github\workflows" mkdir "%REPO%\.github\workflows"
copy /Y "%SRC%.github\workflows\build-apk.yml" "%REPO%\.github\workflows\build-apk.yml" >nul
if exist "%SRC%.gitignore" copy /Y "%SRC%.gitignore" "%REPO%\.gitignore" >nul

echo.
echo [4/9] Restaurando google-services.json...
if exist "%GOOGLE_TMP%" (
  copy /Y "%GOOGLE_TMP%" "%APP_DST%\google-services.json" >nul
  echo OK: google-services.json restaurado em app\google-services.json
) else (
  echo AVISO: Sem google-services.json para restaurar. O build pode compilar, mas Firebase real pode nao conectar.
)

echo.
echo [5/9] Conferindo e corrigindo imports obrigatorios do Compose...
if not exist "%DRIVER%" (
  echo ERRO: DriverHomeScreen.kt nao encontrado em:
  echo %DRIVER%
  pause
  exit /b 1
)

findstr /C:"import androidx.compose.foundation.layout.Column" "%DRIVER%" >nul || powershell -NoProfile -ExecutionPolicy Bypass -Command "$p='%DRIVER%'; $t=Get-Content -Raw -Encoding UTF8 $p; $t=$t -replace 'import androidx\.compose\.foundation\.layout\.Box\r?\n', 'import androidx.compose.foundation.layout.Box`r`nimport androidx.compose.foundation.layout.Column`r`n'; Set-Content -Encoding UTF8 $p $t"
findstr /C:"import androidx.compose.material3.NavigationBarItem" "%DRIVER%" >nul || powershell -NoProfile -ExecutionPolicy Bypass -Command "$p='%DRIVER%'; $t=Get-Content -Raw -Encoding UTF8 $p; $t=$t -replace 'import androidx\.compose\.material3\.NavigationBar\r?\n', 'import androidx.compose.material3.NavigationBar`r`nimport androidx.compose.material3.NavigationBarItem`r`n'; Set-Content -Encoding UTF8 $p $t"
findstr /C:"import androidx.compose.material3.NavigationBarItemDefaults" "%DRIVER%" >nul || powershell -NoProfile -ExecutionPolicy Bypass -Command "$p='%DRIVER%'; $t=Get-Content -Raw -Encoding UTF8 $p; $t=$t -replace 'import androidx\.compose\.material3\.NavigationBarItem\r?\n', 'import androidx.compose.material3.NavigationBarItem`r`nimport androidx.compose.material3.NavigationBarItemDefaults`r`n'; Set-Content -Encoding UTF8 $p $t"

echo.
echo [6/9] Validacao final dos imports aplicados:
findstr /N /C:"import androidx.compose.foundation.layout.Column" "%DRIVER%"
if errorlevel 1 goto import_error
findstr /N /C:"import androidx.compose.material3.NavigationBarItem" "%DRIVER%"
if errorlevel 1 goto import_error
findstr /N /C:"import androidx.compose.material3.NavigationBarItemDefaults" "%DRIVER%"
if errorlevel 1 goto import_error

echo.
echo [7/9] Conferindo se o layout novo esta aplicado...
findstr /C:"Novidades da operacao" "%DRIVER%" >nul || findstr /C:"Novidades da operação" "%DRIVER%" >nul
if errorlevel 1 (
  echo AVISO: Nao achei texto do carrossel novo, mas vou continuar.
) else (
  echo OK: tela nova clara encontrada no DriverHomeScreen.kt.
)

echo.
echo [8/9] Commit e push para GitHub...
git status --short
git add .
git commit -m "Aplicar V5.11 layout claro corrigido" || echo AVISO: nada novo para commitar ou commit ja existente.
git push
if errorlevel 1 (
  echo.
  echo ERRO: git push falhou. Pode ser login/token do GitHub no PC.
  echo Autentique no GitHub Desktop/Git Bash e rode este BAT de novo.
  pause
  exit /b 1
)

echo.
echo [9/9] PRONTO.
echo Abra o GitHub Actions e veja o NOVO build criado agora.
echo Nao use o build antigo #59 se ele aparece como "failed 1 hour ago".
echo.
pause
exit /b 0

:import_error
echo.
echo ERRO: Os imports ainda nao aparecem no arquivo. Nao vou subir quebrado.
echo Arquivo: %DRIVER%
pause
exit /b 1
