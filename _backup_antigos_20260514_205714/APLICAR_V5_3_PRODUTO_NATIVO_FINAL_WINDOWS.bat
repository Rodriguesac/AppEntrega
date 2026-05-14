@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.3 - Produto Nativo Final

echo.
echo ===============================================
echo  RODRIGUES ENTREGADOR V5.3
echo  VISUAL FINAL + STATUS + RADAR + MAPA LIMPO
echo ===============================================
echo.
echo Esta versao aplica:
echo - foto real no topo
echo - status no proprio botao
echo - Indisponivel / Disponivel / Restricao
echo - restricoes operacionais
echo - radar aguardando corridas
echo - icones profissionais no rodape
echo - menu/ajustes organizado na Conta
echo - modo desenvolvedor fora do menu
echo - mapa interno limpo sem camadas sobre a rota
echo.

git add -A
git commit -m "V5.3 produto nativo final status radar mapa limpo"
git push

echo.
echo Finalizado. Agora gere o APK pelo GitHub Actions.
pause
