@echo off
chcp 65001 >nul
title Rodrigues Entregador V5.2.3 - Restaura mapa interno

echo.
echo ===============================================
echo  RODRIGUES ENTREGADOR V5.2.3
echo  RESTAURA MAPA INTERNO + MANTEM CORRECAO FINANCEIRA
echo ===============================================
echo.
echo Este pacote desfaz a retirada do mapa interno da V5.2.2.
echo Mantem:
echo - mapa/rota dentro do app
echo - oferta urgente com rota visual
echo - corrida em andamento com rota visual
echo - botao para abrir navegacao externa
echo - correcao dos valores de repasse da V5.2.1
echo.

git add -A
git commit -m "V5.2.3 restaura mapa interno e mantem repasse correto"
git push

echo.
echo Finalizado. Agora rode o GitHub Actions para gerar o APK.
pause
