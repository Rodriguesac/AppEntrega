@echo off
setlocal

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "NOVO=%~dp0"
set "BRANCH=main"

echo.
echo ==========================================
echo V5.8.1 - CARROSSEL HOME + CORRECAO LOCALE
echo ==========================================
echo.

if not exist "%REPO%\.git" (
  echo ERRO: nao encontrei .git em %REPO%
  echo Ajuste o caminho REPO dentro deste BAT.
  pause
  exit /b 1
)

cd /d "%REPO%" || exit /b 1

echo Criando backup local da branch atual...
git branch backup-antes-v581-carrossel-locale 2>nul

echo.
echo Removendo arquivos versionados antigos...
git rm -r --ignore-unmatch .

echo.
echo Limpando arquivos locais, preservando .git...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-ChildItem -Force '%REPO%' | Where-Object { $_.Name -ne '.git' } | Remove-Item -Recurse -Force"

echo.
echo Copiando projeto novo para o repositorio...
robocopy "%NOVO%" "%REPO%" /E /XD .git /XF "%~nx0"
if %ERRORLEVEL% GEQ 8 (
  echo ERRO no robocopy. Codigo: %ERRORLEVEL%
  pause
  exit /b 1
)

echo.
echo Adicionando arquivos novos ao Git...
cd /d "%REPO%"
git add -A

echo.
echo Commitando...
git commit -m "Corrige Locale e sobe V5.8.1 com carrossel"

echo.
echo Enviando para GitHub...
git push origin %BRANCH%

echo.
echo FINALIZADO. Abra o GitHub Actions e rode o build se ele nao iniciar sozinho.
pause
