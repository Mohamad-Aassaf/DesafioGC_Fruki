package com.fruki.api;

import com.fruki.api.dominio.Cliente;
import com.fruki.api.repositorio.ClienteRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Fluxo ponta a ponta pela API (banco H2 em memória, com a base de demonstração). */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:fruki-teste;DB_CLOSE_DELAY=-1",
        "fruki.clima.fonte=simulado"})
class FrukiApiApplicationTests {

    @Autowired
    WebApplicationContext contexto;
    @Autowired
    ClienteRepository clientes;

    MockMvc api;

    @BeforeEach
    void preparar() {
        api = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    long idDe(String nome) {
        return clientes.findAll().stream().filter(c -> c.getNome().equals(nome)).map(Cliente::getId).findFirst().orElseThrow();
    }

    /** Sufixo só de letras, para nomes únicos que continuam válidos depois da limpeza. */
    static String unico() {
        String s = UUID.randomUUID().toString().replaceAll("[^a-f]", "") + "abcdef";
        return s.substring(0, 6);
    }

    long novoCliente(String segmento) throws Exception {
        String json = """
                {"nome": "Cliente Teste %s", "cidade": "Canoas", "uf": "RS", "segmento": "%s", "vendedor": "Rep. Teste"}
                """.formatted(unico(), segmento);
        String r = api.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(r, "$.registro.id")).longValue();
    }

    // ---------- consulta e motor ----------

    @Test
    void recomendaSoProdutosQueOClienteNaoCompra_doMaiorParaOMenor() throws Exception {
        long id = idDe("Mercado Pinheiro");
        String cliente = api.perform(get("/api/clientes/" + id)).andReturn().getResponse().getContentAsString();
        List<Integer> comprados = JsonPath.read(cliente, "$.produtosComprados");

        String r = api.perform(get("/api/clientes/" + id + "/recomendacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value("v2"))
                .andExpect(jsonPath("$.recomendacoes", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.recomendacoes[0].motivos", hasSize(greaterThan(0))))
                .andReturn().getResponse().getContentAsString();

        List<Integer> recomendados = JsonPath.read(r, "$.recomendacoes[*].produtoId");
        assertThat(recomendados).doesNotContainAnyElementsOf(comprados);
        List<Integer> pontos = JsonPath.read(r, "$.recomendacoes[*].pontos");
        assertThat(pontos.get(0)).isEqualTo(pontos.stream().max(Integer::compare).orElseThrow());
        List<Integer> v1 = JsonPath.read(api.perform(get("/api/clientes/" + id + "/recomendacoes?versao=v1"))
                .andReturn().getResponse().getContentAsString(), "$.recomendacoes[*].pontos");
        assertThat(v1).isSortedAccordingTo((a, b) -> b - a);
    }

    @Test
    void v2LimitaProdutosDaMesmaFamilia() throws Exception {
        String r = api.perform(get("/api/clientes/" + idDe("Lanchonete Nova Era") + "/recomendacoes?limite=6&maxPorFamilia=2"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        List<String> familias = JsonPath.read(r, "$.recomendacoes[*].familia");
        Map<String, Integer> contagem = new HashMap<>();
        familias.forEach(f -> contagem.merge(f, 1, Integer::sum));
        assertThat(contagem.values()).allMatch(n -> n <= 2);
    }

    @Test
    void v1ContinuaDisponivelParaComparacao() throws Exception {
        api.perform(get("/api/clientes/" + idDe("Mercado Pinheiro") + "/recomendacoes?versao=v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value("v1"));
    }

    @Test
    void clienteSemHistorico_recebeSugestoesComAviso() throws Exception {
        api.perform(get("/api/clientes/" + idDe("Lanchonete Nova Era") + "/recomendacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.produtosNoHistorico").value(0))
                .andExpect(jsonPath("$.recomendacoes", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.avisos", hasItem(containsString("sem histórico"))));
    }

    @Test
    void semProdutoAcimaDaPontuacaoMinima_devolveListaVaziaComAviso() throws Exception {
        api.perform(get("/api/clientes/" + idDe("Mercado Pinheiro") + "/recomendacoes?pontuacaoMinima=200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recomendacoes", hasSize(0)))
                .andExpect(jsonPath("$.avisos", hasItem(containsString("pontuação mínima"))));
    }

    @Test
    void clienteInexistente_404() throws Exception {
        api.perform(get("/api/clientes/999999/recomendacoes"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(containsString("não encontrado")));
    }

    @Test
    void parametrosInvalidos_400() throws Exception {
        long id = idDe("Mercado Pinheiro");
        api.perform(get("/api/clientes/" + id + "/recomendacoes?bonusCalor=999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value(containsString("bonusCalor")));
        api.perform(get("/api/clientes/" + id + "/recomendacoes?bonusCalor=abc"))
                .andExpect(status().isBadRequest());
        api.perform(get("/api/clientes/" + id + "/recomendacoes?versao=v9"))
                .andExpect(status().isBadRequest());
        api.perform(get("/api/clientes/abc/recomendacoes"))
                .andExpect(status().isBadRequest());
    }

    // ---------- ingestão ----------

    @Test
    void novoCliente_eLimpoEPadronizado() throws Exception {
        String nome = "  lanchonete   DO " + unico().toUpperCase() + "  ";
        String json = """
                {"nome": "%s", "cidade": "porto  alegre", "uf": " rs ", "segmento": "LANCHERIA"}
                """.formatted(nome);
        api.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registro.nome").value(containsString("Lanchonete do ")))
                .andExpect(jsonPath("$.registro.cidade").value("Porto Alegre"))
                .andExpect(jsonPath("$.registro.uf").value("RS"))
                .andExpect(jsonPath("$.registro.segmento").value("Lanchonete"))
                .andExpect(jsonPath("$.registro.vendedor").value("Sem representante"))
                .andExpect(jsonPath("$.ajustes", hasSize(greaterThan(0))));
    }

    @Test
    void clienteInvalido_400ComTodosOsProblemas() throws Exception {
        api.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "Mercado X", "cidade": "Canoas", "uf": "XX", "segmento": "Banco"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(2)));
        api.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasItem("nome é obrigatório")));
        api.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content("{isso não é json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clienteDuplicado_409() throws Exception {
        api.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "MERCADO PINHEIRO", "cidade": "canoas", "uf": "rs", "segmento": "mercado"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void novaCompra_entraNoHistoricoESaiDasRecomendacoes() throws Exception {
        long id = novoCliente("Padaria");
        String antes = api.perform(get("/api/clientes/" + id + "/recomendacoes")).andReturn().getResponse().getContentAsString();
        int primeiro = JsonPath.read(antes, "$.recomendacoes[0].produtoId");

        api.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": %d, \"produtoId\": %d, \"quantidade\": 10}".formatted(id, primeiro)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registro.data").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.ajustes[0]").value(containsString("data não informada")));

        api.perform(get("/api/clientes/" + id + "/recomendacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.produtosNoHistorico").value(1))
                .andExpect(jsonPath("$.recomendacoes[*].produtoId", everyItem(not(primeiro))));

        // A mesma compra de novo, no mesmo dia, é duplicada.
        api.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": %d, \"produtoId\": %d, \"quantidade\": 10}".formatted(id, primeiro)))
                .andExpect(status().isConflict());
    }

    @Test
    void compraInvalida_400() throws Exception {
        long id = idDe("Mercado Pinheiro");
        api.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": %d, \"produtoId\": 999999, \"quantidade\": 5, \"data\": \"%s\"}"
                                .formatted(id, LocalDate.now().plusDays(3))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes", hasSize(2)));
        api.perform(post("/api/compras").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clienteId\": %d, \"produtoId\": 1, \"quantidade\": 0}".formatted(id)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes[0]").value(containsString("quantidade")));
    }

    @Test
    void lote_aceitaOsValidosERelataOsInvalidos() throws Exception {
        String json = """
                {
                  "produtos": [
                    {"nome": "Chá Mate %s 1L", "familia": "chas e sucos", "preco": 31.999, "saudavel": true},
                    {"nome": "Produto sem família", "familia": "Cerveja", "preco": 10}
                  ],
                  "clientes": [
                    {"nome": "Bar Lote %s", "cidade": "Recife", "uf": "pe", "segmento": "boteco"},
                    {"nome": "Sem UF", "cidade": "Recife", "uf": "", "segmento": "Bar"}
                  ],
                  "compras": [
                    {"clienteId": 999999, "produtoId": 1, "quantidade": 3}
                  ]
                }
                """.formatted(unico(), unico());
        api.perform(post("/api/ingestao/lote").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtosAceitos").value(1))
                .andExpect(jsonPath("$.clientesAceitos").value(1))
                .andExpect(jsonPath("$.comprasAceitas").value(0))
                .andExpect(jsonPath("$.rejeitados", hasSize(3)))
                .andExpect(jsonPath("$.ajustes", hasItem(containsString("preco arredondado para 32.00"))));
    }

    // ---------- indicadores ----------

    @Test
    void indicadoresSaoCalculadosParaV1EV2() throws Exception {
        api.perform(get("/api/clientes/" + idDe("Bar da Praça") + "/recomendacoes"));
        api.perform(get("/api/indicadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.v2.cobertura").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.v2.variedade").value(greaterThan(0.0)))
                .andExpect(jsonPath("$.v2.consultasRegistradas").value(greaterThan(0)))
                .andExpect(jsonPath("$.v2.comprasTestadas").value(greaterThan(100)))
                .andExpect(jsonPath("$.v1.cobertura").value(greaterThan(0.0)));
    }

    @Test
    void restaurarDados_voltaParaABaseDeDemonstracaoComOsMesmosIds() throws Exception {
        novoCliente("Bar");
        api.perform(post("/api/admin/restaurar-dados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientes").value(35));
        assertThat(idDe("Mercado Pinheiro")).isEqualTo(1);
        assertThat(idDe("Lanchonete Nova Era")).isEqualTo(28);
    }

    @Test
    void statusDaApi() throws Exception {
        api.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.clientes").value(greaterThan(20)));
    }
}
