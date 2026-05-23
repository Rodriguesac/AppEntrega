# V6.11.0 Correções C+D+E

## Correção C
- Tela urgente e aba Corridas passam a tratar pagamento do pedido.
- Finalização exige código de entrega de 4 dígitos quando houver.
- Código secreto operacional `48` libera finalização quando autorizado.
- Se houver pagamento pendente, o app pergunta como o cliente pagou: dinheiro, Pix ou cartão/maquininha.
- Para cartão/maquininha, o app permite escolher maquininha e tipo de transação para gerar acerto.
- Finalização grava acerto para conferência do gestor.

## Correção D
- Home refinada: tipografia mais compacta, topo mais próximo da referência, sino com indicador.
- Card de corrida ativa fica entre Ganhos de hoje e Carrossel.
- Carrossel e atalhos permanecem na Home mesmo com corrida ativa.
- Bottom nav usa Mais sem ícone de pessoa.

## Correção E
- Tela Mapa aberta pelo atalho da Home.
- Sem corrida: mapa usa localização local do celular e mostra `((•)) Disponível`.
- Com corrida: mapa vira rota/corrida em andamento.
- Notificações do gestor aparecem no app e também como notificação Android.
- Corrida urgente mantém tela cheia como prioridade e notificação como apoio/fallback.
