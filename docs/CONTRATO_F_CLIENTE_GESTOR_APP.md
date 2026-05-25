
# Correção F — Contrato único Cliente ↔ Gestor ↔ App Entregador

Objetivo: fazer Cliente, Gestor e App Entregador falarem a mesma língua no Firebase.

## Pedido oficial

Coleção: `pedidos/{pedidoId}`

Campos principais:

```js
{
  numeroPedido: 1234,
  codigoPedido: "#1234",
  codigoEntrega: "4821", // 4 dígitos numéricos

  cliente: {
    uid: "...",
    nome: "...",
    telefone: "...",
    email: "..."
  },

  endereco: {
    rua: "...",
    numero: "...",
    bairro: "...",
    complemento: "...",
    cep: "...",
    lat: -20.0,
    lng: -54.0
  },

  valores: {
    subtotal: 0,
    taxaEntrega: 0,
    desconto: 0,
    gorjeta: 0,
    total: 0,
    valorPedido: 0,
    valorReceberCliente: 0
  },

  pagamento: {
    status: "PENDENTE" | "PAGO_ONLINE" | "PAGO_NA_ENTREGA",
    forma: "ONLINE" | "PIX_ONLINE" | "DINHEIRO" | "PIX_ENTREGA" | "CARTAO_MAQUININHA" | "NAO_INFORMADO",
    metodo: "texto legado compatível",
    origem: "CLIENTE_CHECKOUT",
    valorPedido: 0,
    valorReceberCliente: 0,
    precisaReceberNaEntrega: true,
    precisaMaquininha: false,
    precisaTroco: false,
    trocoPara: 0
  },

  status: "RECEBIDO" | "AGUARDANDO_PAGAMENTO" | "EM_PREPARO" | "PRONTO" | "BUSCANDO_ENTREGADOR" | "A_CAMINHO_CLIENTE" | "ENTREGADOR_NO_LOCAL" | "ENTREGUE",
  statusPedidoCore: "RECEBIDO",
  statusPagamento: "PENDENTE",
  statusEntrega: "AGUARDANDO_DESPACHO"
}
```

## Despacho oficial para o app

O gestor deve criar/atualizar:

- `rotas_entrega/{rotaId}` — principal canal que o app escuta para oferta/corrida.
- `ofertasEntregador/{uid}/ofertas/{ofertaId}` — log/apoio da oferta por entregador.
- `pedidos/{pedidoId}` — status e vínculo da rota.

Campos que precisam ir para a rota/oferta:

```js
{
  pedidoId,
  numeroPedido,
  codigoPedido,
  codigoEntrega,
  entregadorUid,
  targetDriverId,
  status: "OFERTA_ENTREGADOR",
  statusOfertaEntregador: "OFERTA_ENTREGADOR",
  statusEntrega: "BUSCANDO_ENTREGADOR",
  valorPedido,
  valorCorrida,
  taxaEntrega,
  pagamento,
  coleta,
  entrega,
  liberadoParaEntregador: true,
  ofertarEntregador: true
}
```

## Notificações

O gestor grava em:

`app_notifications`

O app escuta:

`app_notifications`

Campos:

```js
{
  title,
  message,
  category: "Operação" | "Financeiro" | "Sistema" | "Suporte",
  priority: "NORMAL" | "HIGH" | "URGENT",
  targetGroup: "all" | "drivers" | "financeiro",
  targetUid: null | "uidDoEntregador",
  active: true,
  createdAt
}
```

## Maquininhas

Coleção: `maquininhas`

```js
{
  nome,
  ativa: true,
  dono: "loja" | "motoboy" | "sistema",
  taxaDebito: 2,
  taxaCredito: 3.5,
  taxaParcelado: 4.5,
  taxaTicket: 4
}
```

## Acerto financeiro

Coleções/campos:

- `acertosEntregadores`
- `repassesEntregadores`
- `entregadores/{uid}.acerto`
- `entregadores/{uid}.financeiro`

Exemplo:

```js
{
  entregadorUid,
  periodo,
  recebidoPeloEntregador,
  taxaMotoboy,
  taxasMaquininha,
  valorARepassar,
  valorAReceber,
  dinheiro,
  cartao,
  pix,
  statusAcerto: "AGUARDANDO_CONFERENCIA"
}
```

## Localização do entregador

O app deve atualizar:

```js
entregadores/{uid}: {
  coords: { lat, lng },
  localizacaoAtualizadaEm,
  pedidoAtualId,
  rotaAtualId,
  statusOperacional
}
```

O acompanhamento do cliente lê `entregadores/{entregadorUid}.coords`.
