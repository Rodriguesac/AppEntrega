@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.2.4 - Mapa limpo

echo.
echo ===============================================
echo  RODRIGUES ENTREGADOR V5.2.4
echo  MAPA LIMPO SEM CAMADA POR CIMA
echo ===============================================
echo.
echo Alteracoes principais:
echo - Mapa interno mantido.
echo - Sem cards/textos/legendas por cima do mapa pequeno.
echo - Apenas icone de tela cheia sobre o mapa.
echo - Clique no mapa tambem abre tela cheia.
echo - Tela cheia com pins de coleta, entrega e localizacao do motoboy.
echo - Indo para coleta: rota motoboy ate loja.
echo - Indo para entrega: rota motoboy ate cliente.
echo - Atualizacao da rota/localizacao a cada 30 segundos.
echo.

git add -A
git commit -m "V5.2.4 mapa limpo com tela cheia e rota por etapa"
git push

echo.
echo Finalizado. Agora rode o GitHub Actions para gerar o APK.
pause
