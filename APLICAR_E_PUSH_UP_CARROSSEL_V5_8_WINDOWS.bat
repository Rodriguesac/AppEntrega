@echo off
setlocal
chcp 65001 >nul

echo.
echo ===============================================
echo  UP ENTREGAS V5.8 - TEMA CLARO + CARROSSEL
echo ===============================================
echo.

if not exist app\build.gradle.kts (
  echo ERRO: execute este BAT dentro da pasta raiz do repositorio.
  pause
  exit /b 1
)

echo Limpando backups e arquivos antigos desnecessarios...
for /d %%D in (_backup* backup* backups* antigos* arquivos rodrigues-entregador-nativo-v56-radar-direto-login-pro) do (
  if exist "%%D" rmdir /s /q "%%D"
)
for %%F in (README.md LEIA*.txt *.md APLICAR_E_LIMPAR_V5_*.bat APLICAR_V52_LIMPO_FINAL_E_PUSH.bat) do (
  if exist "%%F" del /f /q "%%F"
)

echo.
echo Verificando Git...
git status

echo.
echo Adicionando alteracoes...
git add .

echo.
echo Criando commit...
git commit -m "Up Entregas V5.8 tema claro e carrossel"

echo.
echo Enviando para o GitHub...
git push

echo.
echo Pronto. Se o workflow estiver ativo, o APK sera gerado em Actions.
pause
