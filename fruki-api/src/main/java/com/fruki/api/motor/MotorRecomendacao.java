package com.fruki.api.motor;

import com.fruki.api.clima.ClimaService;
import com.fruki.api.clima.PrevisaoClima;
import com.fruki.api.dominio.Catalogo;
import com.fruki.api.dominio.Cliente;
import com.fruki.api.dominio.ConsultaRecomendacao;
import com.fruki.api.dominio.Evento;
import com.fruki.api.dominio.Produto;
import com.fruki.api.erro.EntradaInvalidaException;
import com.fruki.api.erro.NaoEncontradoException;
import com.fruki.api.motor.ResultadoRecomendacao.ClienteResumo;
import com.fruki.api.motor.ResultadoRecomendacao.Contexto;
import com.fruki.api.motor.ResultadoRecomendacao.EventoProximo;
import com.fruki.api.motor.ResultadoRecomendacao.Item;
import com.fruki.api.repositorio.ClienteRepository;
import com.fruki.api.repositorio.CompraRepository;
import com.fruki.api.repositorio.ConsultaRecomendacaoRepository;
import com.fruki.api.repositorio.EventoRepository;
import com.fruki.api.repositorio.ProdutoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Motor de recomendação por regras e pontuação (scoring).
 *
 * <p>Fluxo: dados do banco (cliente, histórico, produtos, eventos) + contexto (clima)
 * → pontuação de cada produto que o cliente ainda não compra → corte e ranking → resposta.
 *
 * <p><b>V2 (padrão)</b>, a partir dos testes e do feedback da V1:
 * <ul>
 *   <li>Histórico real: % dos clientes do mesmo segmento que compram o produto (antes era um número simulado).</li>
 *   <li>Cesta: "quem compra X também compra Y", a partir das compras de outros clientes.</li>
 *   <li>Cliente sem histórico ou segmento com poucos dados: usa a média geral e avisa.</li>
 *   <li>Eventos respeitam a região do cliente e valem mais quanto mais perto estão.</li>
 *   <li>Pontuação mínima: produto sem motivo forte não é recomendado.</li>
 *   <li>Variedade: no máximo N produtos da mesma família no topo.</li>
 * </ul>
 * <b>V1</b> continua disponível (?versao=v1) só para comparação.
 */
@Service
public class MotorRecomendacao {

    public static final String HISTORICO = "Histórico e segmento";
    public static final String CESTA = "Cesta";
    public static final String CALOR = "Calor";
    public static final String UMIDADE = "Umidade";
    public static final String EVENTOS = "Eventos";
    public static final String AJUSTE = "Ajuste";

    /** Mínimo de clientes do segmento com histórico para confiar na taxa do segmento. */
    static final int MIN_CLIENTES_SEGMENTO = 3;
    /** Pontos máximos do critério histórico (afinidade de 100%). */
    static final int PONTOS_HISTORICO = 40;
    /** Confiança mínima da regra de cesta ("X% de quem compra A compra B") para contar. */
    static final double CONFIANCA_MINIMA_CESTA = 0.5;
    static final int AJUSTE_ENERGETICO_UMIDO = -8;

    /** Mais pontos primeiro; empate decidido pelo id do produto, para o ranking ser estável. */
    static final Comparator<Pontuacao> ORDEM = Comparator.comparingInt((Pontuacao x) -> x.pontos)
            .reversed()
            .thenComparingLong(x -> x.produto.getId());

    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;
    private final CompraRepository compras;
    private final EventoRepository eventos;
    private final ConsultaRecomendacaoRepository consultas;
    private final ClimaService clima;
    private final int janelaHistoricoDias;

    public MotorRecomendacao(ClienteRepository clientes, ProdutoRepository produtos, CompraRepository compras,
                             EventoRepository eventos, ConsultaRecomendacaoRepository consultas,
                             ClimaService clima, @Value("${fruki.historico.janela-dias:180}") int janelaHistoricoDias) {
        this.clientes = clientes;
        this.produtos = produtos;
        this.compras = compras;
        this.eventos = eventos;
        this.consultas = consultas;
        this.clima = clima;
        this.janelaHistoricoDias = janelaHistoricoDias;
    }

    /** Consulta de um cliente, registrada para o indicador de taxa de recomendação. */
    @Transactional
    public ResultadoRecomendacao recomendar(long clienteId, Parametros parametros, String versao) {
        String v = versao(versao);
        Parametros.Pesos pesos = parametros.resolver();
        BaseHistorica base = carregarBase();
        Cliente cliente = base.clientes.get(clienteId);
        if (cliente == null) throw new NaoEncontradoException("Cliente " + clienteId + " não encontrado");

        ResultadoRecomendacao r = recomendar(base, cliente, pesos, v);
        String ids = r.recomendacoes().stream().map(i -> String.valueOf(i.produtoId())).collect(Collectors.joining(","));
        consultas.save(new ConsultaRecomendacao(clienteId, v, r.recomendacoes().size(), ids));
        return r;
    }

    @Transactional(readOnly = true)
    public BaseHistorica carregarBase() {
        LocalDate hoje = LocalDate.now();
        return new BaseHistorica(
                clientes.findAll(),
                produtos.findByAtivoTrueOrderByIdAsc(),
                eventos.findByDataGreaterThanEqualOrderByDataAsc(hoje),
                compras.findByDataGreaterThanEqual(hoje.minusDays(janelaHistoricoDias)));
    }

    public static String versao(String versao) {
        String v = versao == null || versao.isBlank() ? "v2" : versao.trim().toLowerCase();
        if (!v.equals("v1") && !v.equals("v2")) {
            throw new EntradaInvalidaException("versao deve ser v1 ou v2 (recebido: " + versao + ")");
        }
        return v;
    }

    /** Recomendação sem registrar consulta (usada também pelos indicadores). */
    public ResultadoRecomendacao recomendar(BaseHistorica base, Cliente c, Parametros.Pesos p, String versao) {
        List<String> avisos = new ArrayList<>();
        PrevisaoClima previsao = clima.previsao(c.getUf());
        if (previsao.aviso() != null) avisos.add(previsao.aviso());

        String regiao = Catalogo.regiao(c.getUf());
        LocalDate hoje = LocalDate.now();
        boolean v2 = versao.equals("v2");
        List<Evento> eventosValidos = base.eventos.stream()
                .filter(e -> dias(hoje, e) <= p.janelaEventos())
                .filter(e -> !v2 || e.valeParaRegiao(regiao))
                .toList();

        Set<Long> comprados = base.comprados(c.getId());
        List<Produto> candidatos = base.produtosAtivos.stream().filter(pr -> !comprados.contains(pr.getId())).toList();

        if (comprados.isEmpty()) {
            avisos.add("Cliente sem histórico de compras: sugestões baseadas no segmento, no clima e nos eventos.");
        }
        if (candidatos.isEmpty()) {
            avisos.add("O cliente já compra todos os produtos ativos do portfólio.");
        }

        List<Pontuacao> pontuados = new ArrayList<>();
        if (v2) {
            List<Long> pares = base.outrosComHistorico(c.getId(), c.getSegmento());
            List<Long> todos = base.outrosComHistorico(c.getId(), null);
            boolean segmentoConfiavel = pares.size() >= MIN_CLIENTES_SEGMENTO;
            if (!segmentoConfiavel && !candidatos.isEmpty()) {
                avisos.add("Poucos clientes do segmento " + c.getSegmento() + " com histórico (" + pares.size()
                        + "); a afinidade usa a média de todos os clientes.");
            }
            for (Produto pr : candidatos) {
                pontuados.add(pontuarV2(base, c, pr, p, previsao, eventosValidos, pares, todos, segmentoConfiavel, comprados));
            }
        } else {
            for (Produto pr : candidatos) pontuados.add(pontuarV1(c, pr, p, previsao, eventosValidos));
        }
        pontuados.sort(ORDEM);

        int descartados = 0;
        List<Pontuacao> ranking;
        if (v2) {
            List<Pontuacao> aprovados = new ArrayList<>();
            for (Pontuacao x : pontuados) {
                if (x.pontos() >= p.pontuacaoMinima()) aprovados.add(x);
                else descartados++;
            }
            ranking = diversificar(aprovados, p.limite(), p.maxPorFamilia());
            if (ranking.isEmpty() && !candidatos.isEmpty()) {
                avisos.add("Nenhum produto atingiu a pontuação mínima (" + p.pontuacaoMinima()
                        + "). Nada é recomendado para não sugerir produto sem motivo.");
            }
        } else {
            ranking = pontuados.stream().limit(p.limite()).toList();
        }

        List<Item> itens = new ArrayList<>();
        for (int i = 0; i < ranking.size(); i++) {
            Pontuacao x = ranking.get(i);
            itens.add(new Item(i + 1, x.produto.getId(), x.produto.getNome(), x.produto.getFamilia(),
                    x.produto.getPreco(), x.pontos(), x.partes, x.motivos));
        }

        Contexto contexto = new Contexto(c.getUf(), regiao, previsao.temperaturaMedia(), previsao.umidadeMedia(),
                previsao.classeUmidade(), previsao.fonte(),
                eventosValidos.stream().map(e -> new EventoProximo(e.getNome(), e.getData(), dias(hoje, e), e.getFamilias())).toList());
        ClienteResumo resumo = new ClienteResumo(c.getId(), c.getNome(), c.getCidade(), c.getUf(), c.getSegmento(),
                c.getVendedor(), comprados.size());
        return new ResultadoRecomendacao(versao, resumo, contexto, p, itens, candidatos.size(), descartados,
                avisos, LocalDateTime.now());
    }

    // ---------- V2 ----------

    private Pontuacao pontuarV2(BaseHistorica base, Cliente c, Produto pr, Parametros.Pesos p, PrevisaoClima previsao,
                                List<Evento> eventosValidos, List<Long> pares, List<Long> todos,
                                boolean segmentoConfiavel, Set<Long> comprados) {
        Pontuacao r = new Pontuacao(pr);

        // 1. Histórico: quantos clientes parecidos compram este produto.
        double geral = base.taxaCompra(todos, pr.getId());
        double afinidade;
        String motivo;
        if (segmentoConfiavel) {
            double seg = base.taxaCompra(pares, pr.getId());
            afinidade = 0.7 * seg + 0.3 * geral;
            String s = c.getSegmento().toLowerCase();
            motivo = seg > 0
                    ? pct(seg) + " dos clientes do segmento " + s + " compram"
                    : "ainda não vendido no segmento " + s + " (" + pct(geral) + " de todos os clientes compram)";
        } else {
            afinidade = geral;
            motivo = pct(geral) + " dos clientes compram";
        }
        r.somar(HISTORICO, (int) Math.round(PONTOS_HISTORICO * afinidade * p.pesoHistorico() / 100.0), motivo);

        // 2. Cesta: o produto costuma ser comprado junto com algo que o cliente já compra.
        double melhor = 0;
        Long origem = null;
        for (Long q : comprados) {
            List<Long> compradoresQ = todos.stream().filter(id -> base.comprados(id).contains(q)).toList();
            if (compradoresQ.size() < 2) continue;
            double conf = base.taxaCompra(compradoresQ, pr.getId());
            if (conf > melhor) {
                melhor = conf;
                origem = q;
            }
        }
        if (origem != null && melhor >= CONFIANCA_MINIMA_CESTA) {
            final Long origemId = origem;
            String nomeOrigem = base.produtosAtivos.stream().filter(x -> x.getId().equals(origemId))
                    .map(Produto::getNome).findFirst().orElse("outro produto");
            r.somar(CESTA, (int) Math.round(p.bonusCesta() * melhor), pct(melhor) + " de quem compra " + nomeOrigem + " também compra");
        }

        // 3 e 4. Clima e umidade, como na V1.
        somarClima(r, pr, p, previsao);

        // 5. Eventos da região do cliente; quanto mais perto, mais pontos (100% hoje, 50% no fim da janela).
        LocalDate hoje = LocalDate.now();
        eventosValidos.stream().filter(e -> e.getFamilias().contains(pr.getFamilia())).findFirst().ifPresent(e -> {
            long d = dias(hoje, e);
            int pts = (int) Math.round(p.bonusEvento() * (1 - 0.5 * d / p.janelaEventos()));
            r.somar(EVENTOS, pts, e.getNome() + " em " + d + " d");
        });
        return r;
    }

    /** Reordena para variedade: no máximo {@code maxPorFamilia} por família no topo; os excedentes vão para o fim. */
    static List<Pontuacao> diversificar(List<Pontuacao> ordenados, int limite, int maxPorFamilia) {
        List<Pontuacao> escolhidos = new ArrayList<>();
        List<Pontuacao> sobra = new ArrayList<>();
        Map<String, Integer> porFamilia = new HashMap<>();
        for (Pontuacao x : ordenados) {
            if (escolhidos.size() == limite) break;
            int n = porFamilia.getOrDefault(x.produto.getFamilia(), 0);
            if (n < maxPorFamilia) {
                escolhidos.add(x);
                porFamilia.put(x.produto.getFamilia(), n + 1);
            } else {
                sobra.add(x);
            }
        }
        // Os que excederam a cota da família só entram depois, se ainda faltar item.
        for (Pontuacao x : sobra) {
            if (escolhidos.size() == limite) break;
            escolhidos.add(x);
        }
        return escolhidos;
    }

    // ---------- V1 (mantida para comparação) ----------

    private Pontuacao pontuarV1(Cliente c, Produto pr, Parametros.Pesos p, PrevisaoClima previsao, List<Evento> eventosValidos) {
        Pontuacao r = new Pontuacao(pr);
        int base = Catalogo.hash(c.getNome() + pr.getNome()) % 25 + 30;
        r.somar(HISTORICO, Math.round(base * p.pesoHistorico() / 100f),
                "clientes do segmento " + c.getSegmento().toLowerCase() + " compram");
        somarClima(r, pr, p, previsao);
        LocalDate hoje = LocalDate.now();
        eventosValidos.stream().filter(e -> e.getFamilias().contains(pr.getFamilia())).findFirst()
                .ifPresent(e -> r.somar(EVENTOS, p.bonusEvento(), e.getNome() + " em " + dias(hoje, e) + " d"));
        return r;
    }

    // ---------- comum ----------

    private static void somarClima(Pontuacao r, Produto pr, Parametros.Pesos p, PrevisaoClima previsao) {
        int t = previsao.temperaturaMedia();
        if (t >= p.limiteCalor() && pr.isQuente()) r.somar(CALOR, p.bonusCalor(), "calor (" + t + "°C)");

        String classe = previsao.classeUmidade();
        int u = previsao.umidadeMedia();
        if (pr.isSaudavel() && classe != null) {
            r.somar(UMIDADE, p.bonusUmidade(), "umidade " + classe + " (" + u + "%): hidratação");
        }
        if ("Energético lata".equals(pr.getFamilia()) && "alta".equals(classe)) {
            r.somar(AJUSTE, AJUSTE_ENERGETICO_UMIDO, "calor úmido: energético menos indicado");
        }
    }

    static long dias(LocalDate hoje, Evento e) {
        return ChronoUnit.DAYS.between(hoje, e.getData());
    }

    private static String pct(double x) {
        return Math.round(x * 100) + "%";
    }

    /** Pontuação de um produto em construção. */
    static final class Pontuacao {
        final Produto produto;
        int pontos;
        final Map<String, Integer> partes = new LinkedHashMap<>();
        final List<String> motivos = new ArrayList<>();

        Pontuacao(Produto produto) {
            this.produto = produto;
        }

        int pontos() {
            return pontos;
        }

        void somar(String criterio, int valor, String motivo) {
            if (valor == 0) return;
            partes.put(criterio, valor);
            pontos += valor;
            motivos.add(motivo);
        }
    }
}
