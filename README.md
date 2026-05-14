# Rodrigues Entregador V4 Release Candidate

Versão 4.0.0 RC do app do entregador, 100% nativo em Kotlin + Jetpack Compose.

## Incluído

- Login nativo por CPF/telefone com senha quando existir no cadastro.
- Cadastro nativo de entregador, nascendo pendente para aprovação no painel gestor.
- Sessão salva no aparelho.
- Busca no schema real do gestor: `entregadores`, `rotas_entrega`, `pedidos`, `historicoEntregador`.
- Status online/offline gravado no Firebase.
- Token FCM salvo no cadastro do entregador.
- Escuta de pedido real em tempo real.
- Tela de oferta, aceitar/rejeitar/expirar e finalizar corrida.
- Alerta urgente com canal novo de notificação V4, som WAV real e vibração.
- Navegação externa com preferência: padrão do celular, Google Maps ou Waze.
- Histórico e ganhos básicos vindos do Firebase.
- Conta com criação/alteração de senha e solicitação de alteração de dados ao gestor.

## Antes de enviar para motoboys

1. Gerar APK pelo GitHub Actions.
2. Instalar em pelo menos dois celulares Android.
3. Testar login, online/offline, pedido aberto, app em segundo plano e tela bloqueada.
4. No Android, verificar se o canal "Nova corrida" está com som ativo.

