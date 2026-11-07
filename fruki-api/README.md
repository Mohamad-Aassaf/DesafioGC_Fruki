# Fruki · API de recomendação (Java / Spring Boot)

Backend do MVP: recebe os dados (clientes, produtos, compras), monta o contexto (clima e eventos),
roda o motor de recomendação e devolve o ranking de produtos para cada cliente.

```
App / Postman / tela web
        │  GET /api/clientes/{id}/recomendacoes
        ▼
┌───────────────────────── fruki-api (Spring Boot) ─────────────────────────┐
│ RecomendacaoController ─► MotorRecomendacao                               │
│                              │  1. carrega a base (banco H2)              │
│                              │     clientes, produtos ativos, compras     │
│                              │     dos últimos 180 dias, eventos          │
│                              │  2. contexto: ClimaService (simulado ou    │
│                              │     Open-Meteo, com plano B) + eventos     │
│                              │  3. pontua cada produto que o cliente      │
│                              │     ainda não compra                       │
│                              │  4. corte, variedade e ranking             │
│                              ▼                                            │
│                       ResultadoRecomendacao (JSON) ─► registra a consulta │
│                                                                           │
│ CadastroController ─► IngestaoService: validação → limpeza → regras → H2  │
│ IndicadoresService: roda o motor para todos os clientes (V1 e V2)         │
└───────────────────────────────────────────────────────────────────────────┘
```

## Como executar

Pré-requisito: **Java 21 ou mais novo** (o Java do Android Studio serve). Não precisa instalar Maven nem banco.

```bash
cd fruki-api
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

- Tela de consulta: **http://localhost:8080**
- API: `http://localhost:8080/api/...`
- Banco: http://localhost:8080/h2 (JDBC URL `jdbc:h2:file:./dados/fruki`, usuário `sa`, sem senha)
- Testes automáticos: `./mvnw test`

Na primeira execução a API cria o banco em `fruki-api/dados/` e carrega a base de demonstração
(13 produtos, 35 clientes em 5 carteiras de vendedores, histórico de compras dos últimos 120 dias e eventos).
O que for ingerido continua salvo entre execuções. Para voltar ao início: `POST /api/admin/restaurar-dados`.

Para usar a previsão do tempo real (Open-Meteo, sem chave), troque em `application.properties`:
`fruki.clima.fonte=open-meteo`. Se a chamada falhar, a API usa o clima simulado e avisa na resposta.

## Endpoints principais

| Método | Caminho | O que faz |
|---|---|---|
| GET | `/api/clientes/{id}/recomendacoes` | **Consulta da recomendação**: ranking com pontos por critério, motivos, contexto e avisos |
| GET | `/api/indicadores` | Acerto no teste retroativo, cobertura, variedade, concentração e taxa de recomendação, V1 e V2 lado a lado |
| GET | `/api/vendedores` | Clientes agrupados por representante (com os produtos que já compram) |
| GET | `/api/clientes` · `/api/clientes/{id}` | Clientes (filtro opcional `?vendedor=`) |
| GET | `/api/clientes/{id}/compras` | Histórico de compras do cliente |
| GET | `/api/produtos` | Portfólio |
| GET | `/api/eventos` · `/api/clima/{uf}` | Contexto usado pelo motor |
| POST | `/api/clientes` | **Ingestão** de cliente |
| POST | `/api/produtos` | Ingestão de produto |
| POST | `/api/compras` | Ingestão de compra |
| POST | `/api/ingestao/lote` | Carga em lote (válidos entram, inválidos voltam no relatório) |
| GET | `/api/status` | Saúde da API e totais |
| POST | `/api/admin/restaurar-dados` | Volta para a base de demonstração |

Coleção pronta para o Postman: [`postman/Fruki-API.postman_collection.json`](postman/Fruki-API.postman_collection.json)
(File › Import). As requisições estão em pastas: consulta, indicadores, ingestão, erros tratados.

### Parâmetros opcionais da consulta

`versao` (`v2` padrão, `v1` para comparar) e os pesos do motor:

| Parâmetro | Padrão | Faixa | Significado |
|---|---|---|---|
| `pesoHistorico` | 100 | 0–300 | % aplicado ao critério histórico |
| `bonusCesta` | 10 | 0–50 | pontos máximos de "quem compra X também compra Y" |
| `bonusCalor` | 15 | 0–50 | pontos para produto de calor quando a média passa do limite |
| `limiteCalor` | 28 | 10–45 | temperatura média (°C) que ativa o bônus de calor |
| `bonusUmidade` | 10 | 0–50 | pontos para hidratação com umidade alta (≥75%) ou baixa (≤45%) |
| `bonusEvento` | 12 | 0–50 | pontos máximos de evento próximo |
| `janelaEventos` | 60 | 1–365 | quantos dias à frente olhar os eventos |
| `pontuacaoMinima` | 20 | 0–200 | abaixo disso o produto não é recomendado |
| `limite` | 5 | 1–20 | tamanho do ranking |
| `maxPorFamilia` | 2 | 1–20 | máximo de produtos da mesma família no ranking |

Exemplo: `GET /api/clientes/1/recomendacoes?bonusCalor=25&limite=3`

### Exemplo de resposta (resumida)

`GET /api/clientes/1/recomendacoes` (Mercado Pinheiro, que já compra Guaraná 2L, Cola 2L e Água sem gás):

```json
{
  "versao": "v2",
  "cliente": { "id": 1, "nome": "Mercado Pinheiro", "cidade": "Canoas", "uf": "RS", "segmento": "Mercado",
               "vendedor": "Rep. 33307 (RS)", "produtosNoHistorico": 3 },
  "contexto": { "uf": "RS", "regiao": "S", "temperaturaMedia": 22, "umidadeMedia": 82, "classeUmidade": "alta",
                "fonteClima": "simulado",
                "eventos": [ { "nome": "Black Friday", "data": "2026-11-27", "emDias": 21,
                               "familias": ["Refrigerante PET", "Refrigerante lata"] } ] },
  "recomendacoes": [
    { "posicao": 1, "produtoId": 7, "produto": "Guaraná Zero 2L", "familia": "Refrigerante PET", "preco": 57.00,
      "pontos": 47,
      "partes": { "Histórico e segmento": 21, "Cesta": 6, "Umidade": 10, "Eventos": 10 },
      "motivos": [ "57% dos clientes do segmento mercado compram",
                   "57% de quem compra Água sem gás 1,5 L também compra",
                   "umidade alta (82%): hidratação",
                   "Black Friday em 21 d" ] }
  ],
  "candidatosAvaliados": 10,
  "descartadosPorPontuacao": 4,
  "avisos": []
}
```

## Lógica da recomendação

Para cada produto **ativo** que o cliente **ainda não comprou** nos últimos 180 dias:

```
pontos = histórico   40 × afinidade × pesoHistorico/100
                     afinidade = 70% × (% do segmento que compra) + 30% × (% de todos os clientes que compram)
       + cesta       bonusCesta × confiança de "quem compra X (que o cliente já compra) também compra este"  (se ≥ 50%)
       + calor       bonusCalor, se produto de calor e temperatura média ≥ limiteCalor
       + umidade     bonusUmidade, se produto de hidratação e umidade alta ou baixa
       + evento      bonusEvento × (1 − 0,5 × dias/janela), evento da família e da região do cliente
       − ajuste      8, energético com calor úmido
```

Depois: descarta quem ficou abaixo de `pontuacaoMinima`, ordena por pontos e reordena para variedade:
no máximo `maxPorFamilia` produtos da mesma família no topo; os excedentes vão para o fim da lista.

### O que mudou da V1 para a V2 (e por quê)

| V1 | V2 | Por quê |
|---|---|---|
| "Histórico" era um número pseudoaleatório derivado do nome | % real de clientes do mesmo segmento que compram o produto | A V1 não usava dado nenhum: o ranking mudava se o cliente mudasse de nome |
| — | Critério **cesta** (compras em conjunto) | Usa o que o próprio cliente já compra para personalizar |
| Cliente sem histórico recebia o mesmo cálculo, sem aviso | Usa segmento/média geral e avisa | Caso de negócio explícito (cliente novo) |
| Eventos valiam para o país todo | Respeita a região (ex.: Iemanjá só Sul e Nordeste) e pesa mais quando o evento está perto | Evita sugerir por um evento que não acontece na região do cliente |
| Recomendava qualquer produto, mesmo sem motivo | Pontuação mínima | Melhor não sugerir do que sugerir sem motivo |
| Topo podia ter 3 produtos da mesma família | Máximo 2 por família | Mais variedade, mais chance de SKU novo de verdade |

Resultado na base de demonstração (detalhes em [`../docs/VALIDACAO.md`](../docs/VALIDACAO.md), ao vivo em `/api/indicadores`):

| | V1 | V2 |
|---|---|---|
| Acerto no teste retroativo: esconde cada compra real e vê se ela volta no top 3 (acaso: 52,2%) | 56,8% | **79,4%** |
| Concentração no produto mais sugerido | 19,4% | **13,0%** |
| Clientes com recomendação | 100% | 100% |

## Tratamento de novos dados (ingestão)

Cada registro passa por: **validação de formato** (campos obrigatórios, tamanhos, faixas) →
**limpeza** → **regras de negócio** → gravação. O que a limpeza mudou volta em `ajustes`.

| Campo | Limpeza | Rejeita (400) quando |
|---|---|---|
| nome, cidade | espaços extras, caixa: `"  mercado   SÃO jorge "` → `"Mercado São Jorge"` | vazio ou longo demais |
| uf | `" rs "` → `"RS"` | não é UF brasileira |
| segmento | grafias e sinônimos: `"super mercado"` → `Supermercado`, `"boteco"` → `Bar` | fora da lista de segmentos |
| familia (produto) | sem acento/caixa: `"chas e sucos"` → `Chás e sucos` | família desconhecida |
| preco | arredonda para centavos | ≤ 0 ou > R$ 1000 |
| vendedor | vazio → `"Sem representante"` | — |
| compra.data | vazia → hoje | no futuro ou com mais de 2 anos |
| compra | — | cliente/produto inexistente, produto inativo, quantidade fora de 1–10000 |

Duplicados viram **409**: cliente com mesmo nome+cidade+UF, produto com mesmo nome, compra do mesmo produto
pelo mesmo cliente no mesmo dia. Assim que a compra é gravada, ela já conta no próximo ranking.

## Regras de negócio e exceções

| Situação | Comportamento |
|---|---|
| Cliente sem histórico | 200 com recomendações por segmento/média geral + aviso |
| Segmento com menos de 3 clientes com histórico | usa a média de todos os clientes + aviso |
| Nenhum produto acima da pontuação mínima | 200 com lista vazia + aviso explicando |
| Cliente já compra todo o portfólio | 200 com lista vazia + aviso |
| Cliente inexistente | 404 |
| Entrada inválida (JSON, tipos, faixas, UF, segmento...) | 400 com a lista de todos os problemas |
| Registro duplicado | 409 |
| Falha na API de clima | usa clima simulado, resposta 200 com aviso |
| Erro inesperado | 500 com mensagem genérica; detalhe só no log |

Formato único de erro:
```json
{ "status": 400, "erro": "Bad Request", "mensagem": "Cliente inválido",
  "detalhes": ["uf inválida: 'XX'. Use a sigla do estado, ex.: RS, SP, BA"], "caminho": "/api/clientes" }
```

## Estrutura do código

```
src/main/java/com/fruki/api
├── dominio/       entidades JPA (Cliente, Produto, Compra, Evento, ConsultaRecomendacao) e Catalogo (UFs, segmentos, famílias)
├── repositorio/   Spring Data JPA
├── ingestao/      Normalizador (limpeza), IngestaoService (validação e regras), formatos de entrada/saída
├── motor/         MotorRecomendacao (V1 e V2), Parametros, BaseHistorica, ResultadoRecomendacao
├── clima/         ClimaService (com plano B), ClimaSimulado, ClimaOpenMeteo
├── indicadores/   IndicadoresService
├── web/           controllers e ErroHandler
└── config/        DadosIniciais (base de demonstração) e CORS
src/main/resources/static/index.html   tela de consulta, indicadores e ingestão
src/test/java                          testes do fluxo ponta a ponta e dos casos de erro
```

## Limitações

- **Dados simulados**: clientes, compras e clima são de demonstração, gerados com semente fixa. Os percentuais
  refletem essa base, não o mercado real.
- **Base pequena**: com 35 clientes, um segmento com 3 ou 4 clientes muda bastante a afinidade com uma compra nova.
- **Regras e pesos definidos à mão**: não há aprendizado com o resultado das vendas (se o cliente aceitou a sugestão).
- **Clima por capital**: a previsão real usa a capital da UF, não a cidade do cliente.
- **Eventos fixos por data**: datas móveis (Carnaval, Páscoa) estão aproximadas.
- **Sem autenticação**: qualquer pessoa na rede consegue consultar e ingerir; adequado só para o MVP.
- **Indicadores calculados na hora**: rodam o motor para todos os clientes; com milhares de clientes seria
  preciso pré-calcular.
