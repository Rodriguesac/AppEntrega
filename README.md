# Rodrigues Entregador Nativo

Versão 3.0.0: app Android 100% nativo em Kotlin + Jetpack Compose + Firebase.

Sem WebView, sem Capacitor e sem web app empacotado como interface principal.

## Fluxo

- Login por CPF ou telefone cadastrado no Firestore.
- Sessão salva no celular.
- Status online/offline salvo em `drivers/{driverId}`.
- FCM token salvo no entregador logado.
- Corridas reais lidas de `rides`.
- Histórico salvo em `driverHistory`.
- Tela urgente nativa para nova corrida.

Leia `README_V3_NATIVO.txt` antes de aplicar.
