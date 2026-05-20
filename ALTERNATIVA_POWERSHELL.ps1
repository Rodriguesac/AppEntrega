$ErrorActionPreference = "Stop"
$repo = "C:\RSITE\rodrigues-entregador-nativo"
$src = Split-Path -Parent $MyInvocation.MyCommand.Path
$appSrc = Join-Path $src "app"
$appDst = Join-Path $repo "app"
$tmpGs = Join-Path $env:TEMP "google-services-rodrigues-entregador.json"
$driver = Join-Path $appDst "src\main\java\com\rodriguesacai\entregador\ui\DriverHomeScreen.kt"

Write-Host "UP ENTREGAS RODRIGUES - V5.11 LAYOUT CLARO REAL"
Write-Host "Destino: $repo"

if (!(Test-Path $repo)) { throw "Pasta do repositorio nao existe: $repo" }
if (!(Test-Path (Join-Path $repo ".git"))) { throw "A pasta existe, mas nao tem .git: $repo" }
if (!(Test-Path $appSrc)) { throw "Pasta app do pacote nao encontrada: $appSrc" }

if (Test-Path (Join-Path $appDst "google-services.json")) {
  Copy-Item (Join-Path $appDst "google-services.json") $tmpGs -Force
}

Remove-Item (Join-Path $appDst "build") -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item (Join-Path $repo "build") -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item (Join-Path $repo ".gradle") -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item (Join-Path $appDst "src") -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item (Join-Path $appDst "build.gradle.kts") -Force -ErrorAction SilentlyContinue
Remove-Item (Join-Path $appDst "build.gradle") -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $appDst | Out-Null

robocopy $appSrc $appDst /E /XF google-services.json /XD build .gradle
if ($LASTEXITCODE -ge 8) { throw "Falha no robocopy" }

Copy-Item (Join-Path $src "build.gradle.kts") (Join-Path $repo "build.gradle.kts") -Force
Copy-Item (Join-Path $src "settings.gradle.kts") (Join-Path $repo "settings.gradle.kts") -Force
Copy-Item (Join-Path $src "gradle.properties") (Join-Path $repo "gradle.properties") -Force
New-Item -ItemType Directory -Force (Join-Path $repo ".github\workflows") | Out-Null
Copy-Item (Join-Path $src ".github\workflows\build-apk.yml") (Join-Path $repo ".github\workflows\build-apk.yml") -Force
if (Test-Path (Join-Path $src ".gitignore")) { Copy-Item (Join-Path $src ".gitignore") (Join-Path $repo ".gitignore") -Force }

if (Test-Path $tmpGs) {
  Copy-Item $tmpGs (Join-Path $appDst "google-services.json") -Force
}

if (!(Test-Path $driver)) { throw "DriverHomeScreen.kt nao encontrado: $driver" }
$t = Get-Content -Raw -Encoding UTF8 $driver
foreach ($need in @(
  "import androidx.compose.foundation.layout.Column",
  "import androidx.compose.material3.NavigationBarItem",
  "import androidx.compose.material3.NavigationBarItemDefaults"
)) {
  if ($t -notlike "*$need*") { throw "Import faltando: $need" }
}

Set-Location $repo
git status --short
git add -A
git commit -m "Aplicar V5.11 layout claro real corrigido"
if ($LASTEXITCODE -ne 0) { Write-Host "Nada novo para commit ou commit ja existe." }
git push
if ($LASTEXITCODE -ne 0) { throw "git push falhou" }

Write-Host "PRONTO. Abra GitHub Actions e veja o build NOVO no topo."
Read-Host "Pressione ENTER para sair"
