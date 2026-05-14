@echo off
setlocal EnableExtensions EnableDelayedExpansion

REM ORGANIZAR_PROJETO_V3_NATIVO.bat
REM Use dentro da raiz do projeto:
REM C:\RSITE\rodrigues-entregador-nativo

title Organizar projeto Rodrigues Entregador V3 Nativo

echo.
echo ==========================================
echo  ORGANIZAR PROJETO - V3 100%% NATIVO
echo ==========================================
echo.

REM Confere se esta na pasta certa
if not exist ".git" (
  echo ERRO: Esta pasta nao parece ser um repositorio Git.
  echo Entre em C:\RSITE\rodrigues-entregador-nativo e rode de novo.
  pause
  exit /b 1
)

if not exist "app" (
  echo ERRO: Pasta app nao encontrada.
  echo Entre na raiz correta do projeto e rode de novo.
  pause
  exit /b 1
)

echo Pasta correta detectada.
echo.

REM Cria pasta para guardar arquivos antigos
if not exist "_arquivados_chatgpt" mkdir "_arquivados_chatgpt"
if not exist "_arquivados_chatgpt\bats_antigos" mkdir "_arquivados_chatgpt\bats_antigos"
if not exist "_arquivados_chatgpt\readmes_antigos" mkdir "_arquivados_chatgpt\readmes_antigos"
if not exist "_arquivados_chatgpt\hibrido_removido" mkdir "_arquivados_chatgpt\hibrido_removido"

echo Organizando arquivos antigos da raiz...
echo.

REM Move BATs antigos, mantendo este organizador se estiver na raiz
for %%F in (*.bat) do (
  if /I not "%%~nxF"=="ORGANIZAR_PROJETO_V3_NATIVO.bat" (
    echo Movendo BAT antigo: %%~nxF
    move /Y "%%~fF" "_arquivados_chatgpt\bats_antigos\" >nul
  )
)

REM Move READMEs extras antigos, mantendo README.md
for %%F in (README_*.txt README_V*.txt LEIA-IMPORTANTE.txt) do (
  if exist "%%~fF" (
    echo Movendo README antigo: %%~nxF
    move /Y "%%~fF" "_arquivados_chatgpt\readmes_antigos\" >nul
  )
)

echo.
echo Removendo/arquivando sobras hibridas WebView/Capacitor...
echo.

REM Move assets hibridos se existirem
if exist "app\src\main\assets" (
  echo Arquivando app\src\main\assets
  if exist "_arquivados_chatgpt\hibrido_removido\assets" rmdir /S /Q "_arquivados_chatgpt\hibrido_removido\assets"
  move /Y "app\src\main\assets" "_arquivados_chatgpt\hibrido_removido\assets" >nul
)

REM Move pacote web hibrido se existir
if exist "app\src\main\java\com\rodriguesacai\entregador\web" (
  echo Arquivando pacote web hibrido
  if exist "_arquivados_chatgpt\hibrido_removido\web" rmdir /S /Q "_arquivados_chatgpt\hibrido_removido\web"
  move /Y "app\src\main\java\com\rodriguesacai\entregador\web" "_arquivados_chatgpt\hibrido_removido\web" >nul
)

REM Move session store hibrido se existir
if exist "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt" (
  echo Arquivando DriverSessionStore.kt hibrido
  move /Y "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt" "_arquivados_chatgpt\hibrido_removido\" >nul
)

echo.
echo Limpando arquivos temporarios comuns...
echo.

if exist ".gradle" (
  echo Limpando .gradle local
  rmdir /S /Q ".gradle"
)

if exist "build" (
  echo Limpando build raiz
  rmdir /S /Q "build"
)

if exist "app\build" (
  echo Limpando app\build
  rmdir /S /Q "app\build"
)

echo.
echo ==========================================
echo  ORGANIZACAO FINALIZADA
echo ==========================================
echo.
echo Agora veja o que mudou:
echo.
git status
echo.
echo Se estiver tudo certo, rode:
echo.
echo git add -A
echo git commit -m "Organiza projeto V3 nativo"
echo git push
echo.
pause
endlocal
