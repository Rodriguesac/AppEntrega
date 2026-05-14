RODRIGUES ENTREGADOR - V3.0.0 100% NATIVO

Esta versão abandona o caminho híbrido/WebView.
O app principal é Kotlin + Jetpack Compose + Firebase.

O que mudou:
- Sem WebView, sem Capacitor e sem assets de web app como tela principal.
- Login nativo por CPF ou telefone cadastrado na coleção drivers.
- Não inicia logado em nome fake.
- Sessão salva no próprio app após login real.
- Status online/offline salvo no Firestore.
- Token FCM salvo no entregador logado.
- Escuta de corridas reais em rides/status=pending.
- Aceitar/rejeitar/expirar/finalizar gravando no Firebase.
- Histórico real em driverHistory.
- Tela urgente nativa com contagem regressiva.
- Localização só é solicitada quando o entregador fica online.

Modelo mínimo do entregador no Firestore:
Coleção: drivers
Documento: use o CPF/telefone sem pontuação OU um ID próprio.
Campos recomendados:
{
  "id": "79999999999",
  "name": "Nome do Entregador",
  "phone": "79999999999",
  "cpf": "00000000000",
  "approved": true,
  "blocked": false,
  "verified": true
}

Modelo mínimo de corrida:
Coleção: rides
Documento: qualquer ID
{
  "status": "pending",
  "broadcast": true,
  "value": "R$ 12,50",
  "valueNumber": 12.5,
  "distance": "3,2 km",
  "duration": "18 min",
  "pickup": "Rodrigues Açaí e Cia",
  "dropoff": "Endereço do cliente",
  "customerName": "Cliente",
  "orderCode": "1234",
  "stops": 2
}

Para mandar para entregador específico, use:
{
  "status": "pending",
  "assignedDriverId": "ID_DO_ENTREGADOR"
}

Como aplicar no Windows:
1. Copie o conteúdo deste ZIP para C:\RSITE\rodrigues-entregador-nativo
2. Execute: APLICAR_V3_NATIVO_E_PUSH_WINDOWS.bat
3. Espere o GitHub Actions gerar o APK.
