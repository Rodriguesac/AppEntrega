@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "KT=%REPO%\app\src\main\java\com\rodriguesacai\entregador\ui\DriverHomeScreen.kt"

echo.
echo ============================================================
echo  CORRIGIR IMPORTS COMPOSE + COMMIT + PUSH
echo ============================================================
echo Repositorio: %REPO%
echo Arquivo:     %KT%
echo.

if not exist "%REPO%" (
  echo ERRO: Pasta do repositorio nao encontrada.
  echo Confira se existe: %REPO%
  pause
  exit /b 1
)

if not exist "%KT%" (
  echo ERRO: DriverHomeScreen.kt nao encontrado no caminho esperado.
  echo O app novo precisa estar aplicado antes deste corretivo.
  pause
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "$file = '%KT%';" ^
  "$content = [System.IO.File]::ReadAllText($file, [System.Text.Encoding]::UTF8);" ^
  "$needed = @('import androidx.compose.foundation.layout.Column','import androidx.compose.material3.NavigationBarItem');" ^
  "$added = @();" ^
  "foreach ($imp in $needed) { if ($content -notmatch [regex]::Escape($imp)) { $content = [regex]::Replace($content, '^(package\s+[^\r\n]+\s*)', '$1' + [Environment]::NewLine + $imp + [Environment]::NewLine, 1, [System.Text.RegularExpressions.RegexOptions]::Multiline); $added += $imp } }" ^
  "$utf8NoBom = New-Object System.Text.UTF8Encoding($false);" ^
  "[System.IO.File]::WriteAllText($file, $content, $utf8NoBom);" ^
  "if ($added.Count -gt 0) { Write-Host 'Imports adicionados:'; $added | ForEach-Object { Write-Host (' - ' + $_) } } else { Write-Host 'Imports ja estavam presentes. Nada para adicionar.' }"

if errorlevel 1 (
  echo ERRO: Falha ao corrigir o arquivo Kotlin.
  pause
  exit /b 1
)

cd /d "%REPO%"

echo.
echo Limpando caches locais do Gradle...
if exist "%REPO%\app\build" rmdir /s /q "%REPO%\app\build"
if exist "%REPO%\build" rmdir /s /q "%REPO%\build"
if exist "%REPO%\.gradle" rmdir /s /q "%REPO%\.gradle"

echo.
echo Status antes do commit:
git status --short

echo.
echo Adicionando arquivos ao Git...
git add .

set "MSG=Corrigir imports Compose do layout claro"

echo.
echo Criando commit...
git commit -m "%MSG%"
if errorlevel 1 (
  echo.
  echo Aviso: Talvez nao havia alteracoes para commitar. Vou tentar push mesmo assim.
)

echo.
echo Enviando para o GitHub...
git push
if errorlevel 1 (
  echo.
  echo ERRO: O push falhou. Normalmente e login/token do GitHub no PC.
  echo Abra o GitHub Desktop ou Git Bash, autentique sua conta e rode este BAT de novo.
  pause
  exit /b 1
)

echo.
echo ============================================================
echo  PRONTO: CORRECAO APLICADA E PUSH ENVIADO
echo ============================================================
echo Agora abra o GitHub Actions e aguarde o APK debug.
echo.
pause
