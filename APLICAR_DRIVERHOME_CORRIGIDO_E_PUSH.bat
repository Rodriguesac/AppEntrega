@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "SRC=%~dp0DriverHomeScreen.kt"
set "DST=%REPO%\app\src\main\java\com\rodriguesacai\entregador\ui\DriverHomeScreen.kt"

echo.
echo ============================================================
echo  CORRECAO FORTE DriverHomeScreen + COMMIT + PUSH
echo ============================================================
echo Repositorio: %REPO%
echo Origem:      %SRC%
echo Destino:     %DST%
echo.

if not exist "%REPO%" (
  echo ERRO: Pasta do repositorio nao encontrada.
  echo Caminho esperado: %REPO%
  pause
  exit /b 1
)

if not exist "%SRC%" (
  echo ERRO: Arquivo DriverHomeScreen.kt nao esta junto deste BAT.
  pause
  exit /b 1
)

if not exist "%REPO%\.git" (
  echo AVISO: Nao encontrei .git em %REPO%.
  echo Vou aplicar mesmo assim, mas commit/push podem falhar se nao for repositorio Git.
  echo.
)

if not exist "%REPO%\app\src\main\java\com\rodriguesacai\entregador\ui" (
  echo Criando pasta de UI...
  mkdir "%REPO%\app\src\main\java\com\rodriguesacai\entregador\ui" >nul 2>nul
)

for /f "tokens=1-4 delims=/ " %%a in ('date /t') do set "DATA=%%a-%%b-%%c-%%d"
for /f "tokens=1-3 delims=:,. " %%a in ('time /t') do set "HORA=%%a-%%b-%%c"
set "BK=%TEMP%\DriverHomeScreen_before_full_fix_%RANDOM%.kt"

if exist "%DST%" (
  echo Fazendo backup temporario em:
  echo %BK%
  copy /Y "%DST%" "%BK%" >nul
)

echo.
echo Substituindo DriverHomeScreen.kt pela versao corrigida completa...
copy /Y "%SRC%" "%DST%" >nul
if errorlevel 1 (
  echo ERRO: Falha ao copiar DriverHomeScreen.kt.
  pause
  exit /b 1
)

echo.
echo Conferindo imports obrigatorios...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$f='%DST%';" ^
  "$t=[System.IO.File]::ReadAllText($f,[System.Text.Encoding]::UTF8);" ^
  "$need=@('import androidx.compose.foundation.layout.Column','import androidx.compose.material3.NavigationBarItem','import androidx.compose.material3.NavigationBarItemDefaults');" ^
  "$missing=@(); foreach($n in $need){ if($t -notlike ('*'+$n+'*')){ $missing += $n } };" ^
  "if($missing.Count -gt 0){ Write-Host 'FALTANDO:'; $missing | %% { Write-Host $_ }; exit 2 } else { Write-Host 'OK: imports Column e NavigationBarItem presentes.' }"
if errorlevel 1 (
  echo ERRO: A verificacao dos imports falhou.
  pause
  exit /b 1
)

cd /d "%REPO%"

echo.
echo Limpando caches/build antigos...
if exist "%REPO%\app\build" rmdir /s /q "%REPO%\app\build"
if exist "%REPO%\build" rmdir /s /q "%REPO%\build"
if exist "%REPO%\.gradle" rmdir /s /q "%REPO%\.gradle"

echo.
echo Status antes do commit:
git status --short

echo.
echo Adicionando correcao ao Git...
git add app/src/main/java/com/rodriguesacai/entregador/ui/DriverHomeScreen.kt

set "MSG=Corrigir DriverHomeScreen layout claro"

echo.
echo Criando commit...
git commit -m "%MSG%"
if errorlevel 1 (
  echo.
  echo Aviso: Git nao criou commit. Pode ser porque nao houve alteracao.
  echo Vou tentar push mesmo assim.
)

echo.
echo Enviando para o GitHub...
git push
if errorlevel 1 (
  echo.
  echo ERRO: Push falhou. Normalmente e login/token do GitHub no PC.
  echo Abra o GitHub Desktop ou Git Bash, autentique, e rode este BAT de novo.
  pause
  exit /b 1
)

echo.
echo ============================================================
echo  PRONTO: DriverHomeScreen corrigido e push enviado
echo ============================================================
echo Agora a Action deve sair do erro Column / NavigationBarItem.
echo.
pause
