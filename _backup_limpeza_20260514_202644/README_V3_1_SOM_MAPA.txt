V3.1 - Som, alerta e rota

O que muda:
- Toca som/vibra quando aparece pedido via Firestore na tela inicial.
- O servico online tambem escuta corrida pendente em segundo plano e dispara alerta urgente.
- Canal de notificacao novo urgent_ride_v31 para forcar som mesmo se o canal antigo ficou sem som no Android.
- Card de nova corrida ganhou previa visual de rota.
- Mantem app 100% nativo, sem WebView/Capacitor.

Observacao:
- Se o Android ainda bloquear som, abra Configuracoes do app > Notificacoes > Nova corrida e confirme som permitido.
