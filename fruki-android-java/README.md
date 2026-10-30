# Fruki — Motor de recomendação (Android nativo, Java)

App do vendedor focado no **motor de recomendação** (regras + pontuação) do Desafio GC 2026.

## Como abrir
1. Android Studio → **File › Open** → selecione esta pasta (`fruki-android-java`).
2. Aguarde o Gradle sincronizar e rode no emulador ou no celular (minSdk 26 / Android 8).

Linha de comando: `gradlew assembleDebug` → APK em `app/build/outputs/apk/debug/`.

## O que a tela mostra
- **Parâmetros do motor**: pesos e limites ajustáveis; sugestões e gráficos mudam na hora.
- **Indicadores**: clientes, ganho médio por cliente e SKUs novos sugeridos.
- **Clima**: mapa do Brasil por temperatura e gráfico de 14 dias (chuva, temperatura, umidade).
- **Meus clientes**: 3 sugestões por cliente (trocáveis), meta da semana, gráfico
  "Por que essas sugestões?" e prévia da mensagem no Pede Fruki (WhatsApp).
- **Vendas por família** (toque numa coluna para ver os produtos) e **eventos** próximos.

## Onde está a lógica
- `motor/MotorRecomendacao.java` — pontuação: histórico/segmento + calor + umidade + evento − ajuste.
- `motor/Parametros.java` — pesos padrão.
- `motor/Clima.java` — previsão simulada por UF.
- `data/Dados.java` — clientes, produtos, famílias, eventos e contorno dos estados (simulados).
- `ui/` — gráficos desenhados em `Canvas`, sem bibliotecas externas.

Dados simulados; para produção, troque `Dados`/`Clima` por chamadas à API da Fruki e a um serviço de clima.
