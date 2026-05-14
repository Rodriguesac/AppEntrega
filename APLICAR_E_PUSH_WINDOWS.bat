@echo off
cd /d %~dp0
git status
git add .
git commit -m "V2 PainelUP hibrido"
git push
pause
