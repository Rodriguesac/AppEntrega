# Rodrigues Entregador — V5.1 Recriado Nativo

Versão 100% nativa em Kotlin/Jetpack Compose, sem WebView e sem Capacitor.

## O que mudou nesta versão

- Visual recriado com direção PainelUP/Up Entregas.
- Tema grafite premium, roxo apenas como detalhe, verde para ação/status.
- Sem texto preto em fundo roxo.
- Sem frases redundantes como "Você está offline".
- Tipografia configurada para Montserrat via Google Fonts para Android.
- Mapa real nativo dentro do app com OpenStreetMap/osmdroid.
- Rota real usando TomTom REST quando houver endereço/coordenada suficiente.
- Fallback de mapa com marcadores reais quando a rota não retornar.
- Botão de navegação externa mantém Google Maps/Waze/padrão do celular.
- Login/cadastro/pedidos seguem o schema real do gestor.

## Aplicar no projeto

Copie o conteúdo deste ZIP para:

```bat
C:\RSITE\rodrigues-entregador-nativo
```

Depois rode:

```bat
APLICAR_V5_1_RECRIADO_NATIVO_WINDOWS.bat
```

## Teste recomendado

1. Gerar APK no GitHub Actions.
2. Instalar no celular.
3. Fazer login com entregador aprovado.
4. Ficar online.
5. Criar pedido/rota pelo gestor.
6. Conferir som, tela urgente, card da corrida e mapa real.
7. Aceitar, iniciar navegação e finalizar.

## Observação importante

O mapa usa tiles OpenStreetMap e rota/geocoding TomTom com a chave do projeto. Se o documento da corrida já tiver latitude/longitude, o mapa abre mais rápido e mais preciso. Se tiver só endereço, o app tenta geocodificar pelo TomTom.
