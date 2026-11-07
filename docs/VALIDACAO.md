# Validação da recomendação

Como sabemos que a recomendação faz sentido, o que melhorou da V1 para a V2 e quando ela pode falhar.
Números gerados com a base de demonstração (35 clientes, 13 produtos, 517 compras) e os parâmetros padrão.
Para reproduzir: rode a API e abra `GET /api/indicadores` (ou a seção "Indicadores" em http://localhost:8080).

## 1. Como validamos

1. **Teste retroativo** (o principal): para cada cliente com 2 ou mais produtos, escondemos uma compra real
   de cada vez e perguntamos ao motor se ele recoloca aquele produto no top 3. São 199 compras testadas.
   É um jeito simples de medir se o motor "adivinha" algo que o cliente de fato compra.
2. **Indicadores de cobertura e variedade**: o motor roda para todos os clientes e medimos quantos recebem
   sugestão, quantos produtos diferentes aparecem e o quanto as sugestões se repetem.
3. **Análise de casos**: lemos o ranking e os motivos de cada cliente para ver se a explicação convence um vendedor.

## 2. Resultados V1 × V2

| Indicador | V1 | V2 | Leitura |
|---|---|---|---|
| **Acerto no teste retroativo (top 3)** | 56,8% | **79,4%** | Um sorteio aleatório acertaria 52,2%: a V1 era praticamente um chute; a V2 acerta bem mais |
| Cobertura (clientes com ao menos 1 sugestão) | 100% | 100% | Todos recebem sugestão, inclusive o cliente sem histórico |
| Variedade (produtos que aparecem em alguma sugestão) | 100% (13 de 13) | 100% (13 de 13) | Igual |
| Concentração no produto mais sugerido | 19,4% | **13,0%** | V2 repete menos o mesmo produto |
| Sugestões por cliente | 2,9 | 2,9 | V2 corta produto sem motivo forte, mas na média sugere o mesmo tanto |
| Potencial por cliente (1 caixa de cada sugestão) | R$ 104,83 | R$ 115,03 | |
| Taxa de recomendação (consultas com resultado) | 100% | 100% | Medida nas consultas reais feitas pela API |

Por que a V1 acertava pouco: o critério "histórico e segmento" da V1 era um número derivado do nome
do cliente e do produto, sem relação com compras. Na V2 ele vem do histórico real (veja
[`fruki-api/README.md`](../fruki-api/README.md#o-que-mudou-da-v1-para-a-v2-e-por-quê)).

## 3. Casos em que a recomendação faz sentido

| Cliente | Top 1 da V2 | Por quê convence |
|---|---|---|
| Atacado Recife Sul (PE, 32°C, umidade 76%) | Água sem gás 1,5 L | 100% dos atacados compram, 85% de quem compra Elev também compra, calor e umidade alta pedem hidratação, Ano Novo puxa água |
| Padaria Trigo Bom (RS, umidade 82%) | Água sem gás 1,5 L | 67% das padarias compram, 71% de quem compra Guaraná 350 ml também compra e a umidade está alta. A V1 sugeria Água com gás, que só 33% das padarias compram |
| Minimercado Sol (RS, compra só Cola 2L) | Guaraná 2L | 100% dos mercadinhos compram e 90% de quem compra Cola 2L também compra: a sugestão mais natural possível |
| Lanchonete Nova Era (sem nenhuma compra) | Guaraná 2L | Sem histórico, o motor usa os mais populares entre todos os clientes (82%) e avisa o vendedor |
| Bar da Praça (CE, 31°C) | Água sem gás 1,5 L, Cola 350 ml, Guaraná 2L | Três famílias diferentes, todas com calor e Black Friday ou Ano Novo como motivo |

## 4. Quando pode falhar (limitações observadas)

| Situação | O que acontece | Exemplo | Como mitigamos / próximo passo |
|---|---|---|---|
| Segmento com poucos clientes | Afinidade do segmento não é confiável | Lanchonetes não têm nenhum cliente com histórico → usa a média geral | Aviso na resposta; com a base real, o mínimo de 3 clientes passa a ser atingido |
| Cliente que já compra quase tudo | Sobram 1 ou 2 produtos, e às vezes só 1 passa da pontuação mínima | Supermercado Serrano (11 de 13 produtos) recebe só 2 sugestões | Correto pela regra; com um portfólio real (centenas de SKUs) isso some |
| Regra de cesta com pouca gente | "100% de quem compra X também compra Y" pode vir de 2 clientes | Bar do Gaúcho: "100% de quem compra Elev Zero também compra Água sem gás" | Hoje exigimos 2 compradores e confiança ≥ 50%; com mais dados, subir o mínimo |
| Produto popular domina | Guaraná Zero 2L é o mais sugerido | 13,0% das sugestões | Limite de 2 por família no topo; dá para penalizar o que já aparece demais |
| Dados simulados | As compras foram geradas a partir de perfis por segmento, o que favorece uma lógica baseada em segmento | O acerto de 79,4% é otimista | Repetir o teste retroativo com dados reais da Fruki antes de confiar no número |
| Clima simulado por região | RS fica em ~22°C e nunca ativa o bônus de calor | Clientes do RS não recebem "calor" | `fruki.clima.fonte=open-meteo` usa a previsão real (com plano B se falhar) |
| Evento sem família associada | Não pontua nenhum produto | Consciência Negra | Cadastrar as famílias que cada data puxa com o time comercial |
| Sem retorno de vendas | O motor não sabe se a sugestão foi aceita | — | Registrar aceite/recusa e usar para ajustar os pesos |

## 5. Casos de erro testados automaticamente

Os testes em `fruki-api/src/test` (18 testes, todos passando) cobrem o fluxo ponta a ponta e as exceções:
recomendação só de produtos não comprados, variedade por família, V1 disponível, cliente sem histórico,
lista vazia por pontuação mínima, cliente inexistente (404), parâmetros inválidos (400), limpeza de cliente novo,
cliente inválido e duplicado (400/409), restauração da base, compra nova entrando no histórico e saindo das sugestões, compra
duplicada (409) e inválida (400), carga em lote com itens válidos e inválidos, indicadores e falha da API de clima.
