Rodrigues Entregador - V1.5 Operação Real

Mudanças visíveis desta versão:
- Removeu a cara de simulação da tela principal.
- A tela inicial agora mostra "Aguardando pedido real" quando online.
- Botão de simular corrida saiu do início e do histórico.
- Modo teste ficou escondido em Mais > Modo teste: abrir alerta.
- Fonte forçada para SansSerif/Roboto via MaterialTheme para não puxar fonte estranha do aparelho.
- Histórico virou tela real de operação, sem botão de simulação.
- Versão exibida: v1.5.0 operação real.

Firebase esperado:
Coleção: rides
Documento exemplo:
{
  "status": "pending",
  "value": "R$ 7,00",
  "distance": "3,52 km",
  "duration": "22 min",
  "pickup": "Rodrigues Açaí e Cia",
  "dropoff": "Carandá Bosque",
  "customerName": "Cliente"
}

Quando o entregador estiver online, essa corrida deve aparecer automaticamente como oferta.
