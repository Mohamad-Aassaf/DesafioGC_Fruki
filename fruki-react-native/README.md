# Fruki — App do vendedor (React Native)

App do representante: clientes, sugestões de SKUs com o motivo de cada uma, mapa por região, clima, vendas e eventos.
Na web ocupa a tela toda, com menu lateral; no celular (ou janela estreita), com barra de abas embaixo.
Quando a API Java ([`../fruki-api`](../fruki-api)) está rodando, clientes e rankings vêm dela (motor V2).
Se a API não responder, o app usa o motor local com dados de demonstração e mostra um aviso no topo.

## Como rodar

```bash
npm install
npm run web          # prévia no navegador (http://localhost:5173), sem Android SDK
npm start            # Metro, para o Android
npm run android      # em outro terminal, com emulador ou celular conectado
```

Endereço da API: `src/api/fruki.ts` (`API_URL`). O emulador Android acessa o computador por `10.0.2.2:8080`
e a web por `localhost:8080`. Num celular de verdade, troque pelo IP do computador na rede.

No Android Studio: **File › Open** → pasta `android/` (o Metro precisa estar rodando).
Na primeira compilação o Android Studio baixa o NDK 27.1 que o React Native exige.

## Estrutura
- `App.tsx` — abre o app do vendedor.
- `src/api/fruki.ts` — conexão com a API (status, clientes, produtos, ranking), com plano B para o motor local.
- `src/screens/MotorScreen.tsx` — navegação (menu lateral / barra inferior), escolha da carteira e janelas de troca e WhatsApp.
- `src/screens/estado.ts` — estado compartilhado: carteira, rankings, sugestões trocadas e visitas do dia.
- `src/screens/abas/` — uma tela por aba:
  - `Inicio.tsx`: resumo do dia, por onde começar, clima e próximas datas.
  - `Clientes.tsx`: busca, filtro por segmento, sugestões com motivos, o que o cliente já compra.
  - `Mapa.tsx`: Brasil por região, com tour animado que começa pelo Sul; previsão de 14 dias por estado.
  - `Vendas.tsx`: vendas por família e calendário comercial.
  - `Ajustes.tsx`: pesos do motor.
- `src/components/MapaRegioes.tsx` — mapa com zoom animado por região e nº de clientes por estado.
- `src/motor/motor.ts` — motor local (V1), usado quando a API está fora do ar.
- `src/motor/clima.ts` — previsão simulada por UF.
- `src/data/dados.ts` — dados simulados.
- `src/components/Graficos.tsx` — gráfico de clima, colunas e composição da pontuação (`react-native-svg`).
- `src/theme.ts` — cores Fruki.
- `web/` e `vite.config.mts` — prévia web (react-native-web + Vite).
