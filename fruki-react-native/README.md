# Fruki — Motor de recomendação (React Native)

Mesmo motor do app Java, em React Native 0.88 + TypeScript.

## Como rodar
```bash
npm install
npm start            # Metro
npm run android      # em outro terminal, com emulador ou celular conectado
```
No Android Studio: **File › Open** → pasta `android/` (o Metro precisa estar rodando).
Na primeira compilação o Android Studio baixa o NDK 27.1 que o React Native exige.

## Estrutura
- `App.tsx` — abre direto o motor.
- `src/screens/MotorScreen.tsx` — parâmetros, indicadores, clima, clientes/sugestões, vendas e eventos.
- `src/motor/motor.ts` — pontuação (histórico/segmento + calor + umidade + evento − ajuste).
- `src/motor/clima.ts` — previsão simulada por UF.
- `src/data/dados.ts` — dados simulados.
- `src/components/Graficos.tsx` — mapa, gráfico de clima, colunas e composição da pontuação (`react-native-svg`).
- `src/theme.ts` — cores Fruki.
