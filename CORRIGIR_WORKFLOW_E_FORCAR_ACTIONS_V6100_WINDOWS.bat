@echo off
setlocal EnableExtensions EnableDelayedExpansion
title Corrigir Workflow V6.10.0 e Forcar Actions

set "REPO=C:\RSITE\rodrigues-entregador-nativo"
set "WF=%REPO%\.github\workflows\build-apk.yml"

echo.
echo ============================================================
echo  CORRIGIR WORKFLOW + FORCAR ACTIONS V6.10.0
echo ============================================================
echo.

if not exist "%REPO%\.git" (
  echo ERRO: nao achei .git em:
  echo %REPO%
  pause
  exit /b 1
)

cd /d "%REPO%" || exit /b 1

echo Criando pasta .github\workflows...
mkdir "%REPO%\.github\workflows" 2>nul

echo Escrevendo build-apk.yml...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
"$yml = @'
name: Build APK Nativo Entregador

on:
  push:
    branches:
      - main
  workflow_dispatch:

permissions:
  contents: read

jobs:
  build:
    name: Gerar APK Debug
    runs-on: ubuntu-latest
    timeout-minutes: 35

    steps:
      - name: Baixar repositorio
        uses: actions/checkout@v4

      - name: Configurar Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '17'

      - name: Configurar Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Configurar Android SDK
        uses: android-actions/setup-android@v3
        with:
          accept-android-sdk-licenses: true
          log-accepted-android-sdk-licenses: false

      - name: Aceitar licencas Android
        shell: bash
        run: |
          set +e
          yes | sdkmanager --licenses >/dev/null
          set -e

      - name: Conferir estrutura
        shell: bash
        run: |
          echo "Raiz do projeto:"
          ls -la
          echo ""
          echo "Workflow:"
          ls -la .github/workflows || true
          echo ""
          echo "Versao:"
          grep -R "versionCode\|versionName" app/build.gradle.kts || true
          test -f settings.gradle.kts || test -f settings.gradle
          test -f app/build.gradle.kts || test -f app/build.gradle

      - name: Gerar APK Debug
        shell: bash
        run: |
          if [ -f "./gradlew" ]; then
            chmod +x ./gradlew
            ./gradlew --no-daemon clean :app:assembleDebug --rerun-tasks --stacktrace
          else
            gradle --no-daemon clean :app:assembleDebug --rerun-tasks --stacktrace
          fi

      - name: Listar APK
        shell: bash
        run: |
          find app/build/outputs/apk -type f -name "*.apk" -print

      - name: Enviar APK
        uses: actions/upload-artifact@v4
        with:
          name: Rodrigues-Entregador-V6-10-0-correcoes-a-b-debug-apk
          path: app/build/outputs/apk/debug/*.apk
          if-no-files-found: error
'@; Set-Content -LiteralPath '%WF%' -Value $yml -Encoding UTF8"

if not exist "%WF%" (
  echo ERRO: workflow nao foi criado.
  pause
  exit /b 1
)

echo.
echo Workflow criado:
dir "%REPO%\.github\workflows"

echo.
echo Status:
git status --short

echo.
echo Commitando workflow e forcando build...
git add -A
git commit -m "Restaura workflow Actions V6.10.0"
if errorlevel 1 (
  git commit --allow-empty -m "Forca build Actions V6.10.0"
)

echo.
echo Enviando para GitHub...
git push origin main
if errorlevel 1 (
  echo ERRO: push falhou.
  pause
  exit /b 1
)

echo.
echo ============================================================
echo  PRONTO
echo ============================================================
echo Abra GitHub > Actions. Deve rodar Build APK Nativo Entregador.
echo.
pause
