# Rodrigues Entregador — V2 PainelUP Híbrido

Esta versão troca a base visual simples nativa pela base avançada do PainelUP enviada como referência.

## O que entra nesta entrega

- App Android em WebView seguro usando `https://appassets.androidplatform.net`.
- Interface e fluxo do PainelUP embarcados em `app/src/main/assets/public`.
- Login não é fake: se não houver sessão salva, abre o login do PainelUP.
- Sessão do entregador fica salva no WebView/localStorage do próprio aparelho.
- Ponte nativa `RodriguesNative` para Android.
- Token FCM nativo salvo e sincronizado em `entregadores/{id}` e `rastreioEntregador/{id}` quando o usuário loga.
- Firebase Web do PainelUP continua funcionando dentro do app.
- Notificação urgente nativa preparada para abrir tela cheia em corrida real.
- Aceitar/Rejeitar pela tela cheia grava status em `rotas_entrega`, `pedidos` e histórico básico.
- Upload de imagem via input file funcionando pelo WebView.
- Geolocalização liberada só quando a página pedir localização.
- Não pede câmera automaticamente.

## Como testar

1. Substitua os arquivos do repositório por esta pasta.
2. Rode:

```bat
git status
git add .
git commit -m "V2 PainelUP hibrido"
git push
```

3. No GitHub Actions, baixe o artifact do APK.
4. Instale no celular.
5. Abra o app: ele deve iniciar no fluxo de login/sessão do PainelUP.

## Observação importante

O arquivo enviado como referência era um APK compilado, não o código-fonte original do web app. Por isso esta V2 reaproveita os assets web compilados do PainelUP e os empacota em um Android novo, com ponte nativa para FCM/tela cheia.
