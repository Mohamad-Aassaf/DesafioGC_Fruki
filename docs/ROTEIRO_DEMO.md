# Roteiro de demonstração (≈ 5 minutos)

Antes de começar: rode a API (`cd fruki-api && mvnw spring-boot:run`), abra http://localhost:8080 e,
se for mostrar o app, `cd fruki-react-native && npm run web`. Para começar do zero:
`POST /api/admin/restaurar-dados` (está na coleção do Postman, pasta 5).

| Tempo | Momento | O que mostrar |
|---|---|---|
| 0:00 | **Problema** | O vendedor visita dezenas de pontos de venda e não sabe qual SKU novo oferecer a cada um. Sugestão genérica não convence o lojista |
| 0:40 | **Solução** | Motor que cruza histórico do segmento, cesta de compras, clima e eventos, e explica o motivo de cada sugestão. Fluxo: dados → API Java → motor → ranking → app |
| 1:20 | **Consulta** | Tela web, cliente *Minimercado Sol*: Guaraná 2L em 1º, porque "100% dos mercadinhos compram · 90% de quem compra Cola 2L também compra". Mostrar o JSON ("ver JSON") ou o Postman |
| 2:00 | **V1 × V2** | Botão "Comparar V1 × V2" no mesmo cliente. A V1 dava pontos por um número sem relação com compras; a V2 usa o histórico real |
| 2:30 | **Ingestão ao vivo** | Cadastrar `"  lanchonete   DO zé "`, `porto alegre`, `rs`, `lancheria`: aparece limpo como "Lanchonete do Zé", RS, Lanchonete. Consultar: recebe sugestões com aviso "sem histórico". Registrar uma compra do 1º produto e consultar de novo: o produto sai da lista |
| 3:20 | **Erros tratados** | UF "XX" → 400 com o motivo; cliente 999999 → 404; cliente repetido → 409 (Postman, pasta 4) |
| 3:50 | **Resultados** | Seção Indicadores: acerto no teste retroativo de 57% → 79% (o acaso daria 52%), concentração 19,4% → 13,0%, cobertura 100% |
| 4:20 | **App** | App do vendedor ligado à API ("API Java · motor V2" no menu). Início: por onde começar o dia. Mapa: "Tour pelas regiões", do Sul (onde estão os principais clientes) ao Norte. Clientes: sugestões com motivos, trocar sugestão e prévia no WhatsApp |
| 4:40 | **Limitações e próximos passos** | Dados simulados (o acerto é otimista), segmentos pequenos, clima por capital. Próximo passo: dados reais e retorno de aceite das sugestões |
