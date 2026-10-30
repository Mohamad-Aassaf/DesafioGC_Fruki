# DesafioGC_Fruki

Motor de recomendação de produtos para os clientes da Fruki Bebidas (Desafio GC 2026 — IA, Dados e Java).

O motor usa **regras e pontuação (scoring)** e cruza três informações de cada cliente:
1. **Histórico e perfil do mercado**: afinidade com clientes do mesmo segmento (padaria, mercado, bar…).
2. **Clima**: previsão de 14 dias do estado do cliente (calor e umidade).
3. **Eventos e feriados**: datas próximas que puxam cada família de produtos.

O resultado é uma lista ordenada dos produtos que o cliente **ainda não compra**, com o motivo de cada
sugestão, um gráfico da composição da pontuação e uma prévia da mensagem no Pede Fruki (WhatsApp).
O vendedor ajusta os pesos de cada critério na tela, e as sugestões mudam na hora.

## Projetos
| Pasta | Tecnologia | Como abrir |
|---|---|---|
| [`fruki-android-java`](fruki-android-java) | Android nativo em **Java** | Android Studio → File › Open nesta pasta |
| [`fruki-react-native`](fruki-react-native) | **React Native** 0.88 + TypeScript | `npm install` e `npm run android` |

### Pontuação
```
pontos = histórico/segmento × peso
       + bônus de calor      (média ≥ limite e produto de calor)
       + bônus de umidade    (umidade alta ou baixa e produto de hidratação)
       + bônus de evento     (evento da família dentro da janela de dias)
       − ajuste              (energético com calor úmido)
```

Os dados de clientes, vendas e clima são **simulados** para a demonstração.
