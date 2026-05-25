# V6.10.2 — Fluxo inicial, pedido, pagamento e código

## Primeira abertura
- O app não pede mais notificação automaticamente ao abrir.
- Mostra tela de boas-vindas com checklist de permissões.
- Quando o Android permite solicitação direta, o app usa botão de permissão.
- Quando só dá por configuração, o app abre a tela adequada.
- Depois segue para login/cadastro.

## Splash de sessão
- Após login ou ao reabrir com sessão salva, o app mostra splash curto enquanto carrega perfil, banners, corrida, carteira e avisos.

## Home
- Home não mostra mais mapa/corrida completa.
- Se houver oferta ou corrida ativa, mostra apenas um atalho compacto para a aba Corridas.
- A operação real de corrida fica na aba Corridas.

## Tela urgente
- Exibe resumo de pagamento do pedido:
  - Pago online
  - Receber dinheiro
  - Pix
  - Cartão/maquininha
  - Pagamento não informado
- Não inventa dados. Quando o pedido não informar pagamento, mostra alerta operacional.

## Entrega e código
- Ao chegar no cliente, o app muda para etapa No cliente.
- Para finalizar, exige código numérico de 4 dígitos quando o pedido trouxer `codigoEntrega`.
- Código secreto operacional: `48` finaliza mesmo quando o código real não estiver disponível ou houver autorização da operação.
- Se não houver código, o app informa que o pedido não trouxe código e orienta ocorrência/uso autorizado.

## Campos que o app escuta
- `codigoEntrega`, `codigoConfirmacaoEntrega`, `deliveryCode`, `codigoCliente`, `pinEntrega`, `pin`
- `formaPagamento`, `pagamento`, `paymentMethod`, `metodoPagamento`
- `statusPagamento`, `pagamentoStatus`, `paymentStatus`, `statusDoPagamento`
- `valorReceberCliente`, `valorCobrarCliente`, `trocoValorCobrar`, `cobrarDoCliente`
- `trocoPara`, `troco`, `valorTrocoPara`, `changeFor`
- `precisaMaquininha`, `maquininhaNecessaria`, `requiresMachine`, `cartaoPresencial`
