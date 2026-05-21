@echo off
setlocal
chcp 65001 >nul
echo.
echo Rodrigues Entregador Nativo V16 - APK unico e UI padrao
echo Este BAT NAO faz commit e NAO faz push.
echo.
if not exist app (
  echo ERRO: execute este BAT dentro da pasta do repositorio rodrigues-entregador-nativo.
  pause
  exit /b 1
)
echo Limpando docs/arquivos soltos antigos...
for %%F in (README.md LEIA*.txt *.md) do if exist "%%F" del /f /q "%%F"
echo.
echo Pronto. Agora rode:
echo git status
echo git add -A
echo git commit -m "Rodrigues Entregador V16 APK unico"
echo git push origin main
echo.
pause
