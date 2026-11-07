# DesafioGC_Fruki

Motor de recomendação de produtos para os clientes da Fruki Bebidas (Desafio GC 2026 — IA, Dados e Java).

Para cada ponto de venda, o sistema sugere os SKUs que o cliente **ainda não compra**, em ordem, com o
**motivo** de cada sugestão. O motor usa regras e pontuação (scoring) e cruza:

1. **Histórico**: quantos clientes do mesmo segmento (padaria, mercado, bar…) compram o produto.
2. **Cesta**: "quem compra X, que o cliente já compra, também compra Y".
3. **Clima**: previsão de 14 dias do estado do cliente (calor e umidade).
4. **Eventos**: feriados e datas comerciais próximas na região do cliente.

## Arquitetura

```
┌──────────────────────┐     HTTP/JSON     ┌──────────────────────────── fruki-api (Java 21, Spring Boot) ──┐
│ App React Native     │ ───────────────►  │ Ingestão: validação → limpeza → regras → banco H2              │
│ (Android / web)      │                   │ Contexto: clima (simulado ou Open-Meteo, com plano B) + eventos │
│ Tela web da API      │ ◄───────────────  │ Motor V2: pontuação → corte → variedade → ranking              │
│ Postman              │   ranking +       │ Indicadores: cobertura, variedade, acerto retroativo           │
└──────────────────────┘   motivos         └─────────────────────────────────────────────────────────────────┘
```

Se a API estiver fora do ar, o app continua funcionando com o motor local e avisa o vendedor.

## Projetos

| Pasta | O que é | Como rodar |
|---|---|---|
| [`fruki-api`](fruki-api) | **API Java (Spring Boot)**: ingestão, persistência, motor V2, indicadores e tela web de consulta | `cd fruki-api` e `mvnw spring-boot:run` → http://localhost:8080 |
| [`fruki-react-native`](fruki-react-native) | App do vendedor (React Native 0.88 + TypeScript), ligado à API | `npm install`, depois `npm run web` (navegador) ou `npm run android` |
| [`fruki-android-java`](fruki-android-java) | Primeira versão do app, Android nativo em Java (motor local V1) | Android Studio → File › Open nesta pasta |

## Como executar o MVP (passo a passo)

1. **API**: precisa de Java 21+.
   ```bash
   cd fruki-api
   mvnw spring-boot:run          # Linux/Mac: ./mvnw spring-boot:run
   ```
   Abra http://localhost:8080 para consultar recomendações, ver os indicadores e cadastrar dados.
   Na primeira execução, a base de demonstração é carregada automaticamente.
2. **Postman** (opcional): importe [`fruki-api/postman/Fruki-API.postman_collection.json`](fruki-api/postman/Fruki-API.postman_collection.json).
3. **App**: com a API rodando,
   ```bash
   cd fruki-react-native
   npm install
   npm run web                   # abre no navegador em http://localhost:5173
   ```
   No menu do app aparece "API Java · motor V2". Para Android: `npm run android`
   com um emulador aberto (o app acessa a API em `10.0.2.2:8080`).
4. **Testes**: `cd fruki-api && mvnw test` (18 testes do fluxo completo e dos casos de erro).

## Documentação

- [`fruki-api/README.md`](fruki-api/README.md): endpoints, parâmetros, lógica da recomendação, o que mudou da V1
  para a V2, tratamento de dados, regras de exceção e limitações.
- [`docs/VALIDACAO.md`](docs/VALIDACAO.md): resultados (V1 × V2), casos em que a recomendação acerta e quando pode falhar.
- [`docs/PENDENCIAS.md`](docs/PENDENCIAS.md): bugs, ajustes e itens de documentação para a Sprint 4.
- [`docs/ROTEIRO_DEMO.md`](docs/ROTEIRO_DEMO.md): roteiro da demonstração do pitch.

## Resultados (base de demonstração)

| | V1 | V2 |
|---|---|---|
| Acerto no teste retroativo (compra real escondida volta no top 3; o acaso daria 52,2%) | 56,8% | **79,4%** |
| Clientes com recomendação | 100% | 100% |
| Concentração no produto mais sugerido | 19,4% | **13,0%** |

Os dados de clientes, compras e clima são **simulados** para a demonstração; os números são ilustrativos
e devem ser refeitos com dados reais.
