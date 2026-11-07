# Pendências para a Sprint 4

Nenhuma funcionalidade essencial falta iniciar: o fluxo dados → recomendação → ranking → aplicação
funciona de ponta a ponta. O que sobra são ajustes, polimento e documentação.

## Ajustes técnicos

- [ ] **App Android em Java** (`fruki-android-java`) ainda usa o motor local (V1). O app que consome a API é
      o React Native. Decidir: integrar o Java à API também ou apresentar o React Native como app oficial.
- [ ] **Endereço da API no app** está fixo (`10.0.2.2` no emulador, `localhost` na web). Para celular de verdade,
      é preciso editar `API_URL` em `fruki-react-native/src/api/fruki.ts`. Ideal: tela de configuração ou variável de build.
- [ ] **Build Android do React Native** não foi validado no computador de desenvolvimento por falta do NDK
      (download pendente). O app foi validado na prévia web (`npm run web`) ligado à API.
- [ ] Expor no app os parâmetros novos da V2 (`bonusCesta`, `pontuacaoMinima`, `maxPorFamilia`), hoje só pela API.
- [ ] "Trocar sugestão" no app com a API mostra até 12 produtos (o limite pedido); avaliar se precisa de mais.
- [ ] Regra de cesta: subir o mínimo de compradores (hoje 2) quando houver dados reais.
- [ ] Datas móveis (Carnaval, Páscoa, Black Friday) estão aproximadas; calcular pela regra de cada ano.
- [ ] Indicadores são calculados na hora (roda o motor para todos os clientes); com base grande, pré-calcular.

## Fora do escopo do MVP (registrar como próximos passos)

- Autenticação na API (hoje aberta, só para demonstração).
- Registrar se o cliente aceitou ou não a sugestão e usar isso para calibrar os pesos.
- Trocar os dados simulados por uma extração real (clientes, pedidos) e repetir a validação.

## Documentação e apresentação

- [ ] Slides do pitch (problema → solução → demonstração → resultados → limitações), usando os números de
      [`VALIDACAO.md`](VALIDACAO.md).
- [ ] Ensaiar o [roteiro de demonstração](ROTEIRO_DEMO.md) cronometrando o tempo.
- [ ] Gravar um vídeo curto da demonstração, como plano B se algo falhar ao vivo.
- [ ] Atualizar `fruki-android-java/README.md` com a decisão sobre o app Java.
