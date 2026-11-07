package com.fruki.api.indicadores;

import com.fruki.api.dominio.Cliente;
import com.fruki.api.motor.BaseHistorica;
import com.fruki.api.motor.MotorRecomendacao;
import com.fruki.api.motor.Parametros;
import com.fruki.api.motor.ResultadoRecomendacao;
import com.fruki.api.repositorio.ConsultaRecomendacaoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Métricas simples da solução, calculadas rodando o motor para todos os clientes. */
@Service
public class IndicadoresService {

    /** O app mostra 3 sugestões por cliente; os indicadores olham esse mesmo topo. */
    public static final int TOP_N = 3;

    public record Indicadores(
            String versao,
            int clientes,
            int clientesComRecomendacao,
            /* % de clientes com pelo menos 1 recomendação */
            double cobertura,
            int produtosAtivos,
            int produtosDistintosRecomendados,
            /* % do portfólio ativo que aparece em alguma recomendação */
            double variedade,
            String produtoMaisRecomendado,
            /* % das sugestões ocupadas pelo produto mais recomendado (quanto menor, menos repetitivo) */
            double concentracao,
            double mediaRecomendacoesPorCliente,
            /* soma do preço (1 caixa) das sugestões, em média por cliente */
            BigDecimal potencialMedioPorCliente,
            int clientesSemHistorico,
            /* teste retroativo: % das compras reais escondidas que o motor recolocou no top 3 */
            double acertoTop3,
            int comprasTestadas,
            long consultasRegistradas,
            long consultasComResultado,
            /* % das consultas feitas pela API que devolveram pelo menos 1 produto */
            Double taxaRecomendacao) {}

    public record Comparativo(LocalDateTime geradoEm, int topN, Indicadores v2, Indicadores v1,
                              Map<String, String> significado) {}

    private final MotorRecomendacao motor;
    private final ConsultaRecomendacaoRepository consultas;

    public IndicadoresService(MotorRecomendacao motor, ConsultaRecomendacaoRepository consultas) {
        this.motor = motor;
        this.consultas = consultas;
    }

    public Comparativo comparar() {
        BaseHistorica base = motor.carregarBase();
        Map<String, String> significado = new LinkedHashMap<>();
        significado.put("cobertura", "% de clientes que recebem pelo menos 1 recomendação");
        significado.put("variedade", "% dos produtos ativos que aparecem em alguma recomendação");
        significado.put("concentracao", "% das sugestões ocupadas pelo produto mais recomendado (menor = menos repetitivo)");
        significado.put("acertoTop3", "teste retroativo: esconde cada compra real e mede quantas o motor recoloca no top 3");
        significado.put("taxaRecomendacao", "% das consultas feitas na API que devolveram produtos");
        significado.put("potencialMedioPorCliente", "soma do preço de 1 caixa de cada sugestão, em média por cliente (R$)");
        return new Comparativo(LocalDateTime.now(), TOP_N, calcular(base, "v2"), calcular(base, "v1"), significado);
    }

    Indicadores calcular(BaseHistorica base, String versao) {
        Parametros.Pesos padrao = Parametros.PADRAO;
        Parametros.Pesos pesos = new Parametros.Pesos(padrao.pesoHistorico(), padrao.bonusCalor(), padrao.limiteCalor(),
                padrao.bonusUmidade(), padrao.bonusEvento(), padrao.janelaEventos(), padrao.bonusCesta(),
                padrao.pontuacaoMinima(), TOP_N, padrao.maxPorFamilia());

        int total = 0, comRecomendacao = 0, semHistorico = 0, sugestoes = 0;
        BigDecimal potencial = BigDecimal.ZERO;
        Map<String, Integer> vezes = new HashMap<>();
        for (Cliente c : base.todosClientes()) {
            ResultadoRecomendacao r = motor.recomendar(base, c, pesos, versao);
            total++;
            if (r.cliente().produtosNoHistorico() == 0) semHistorico++;
            if (!r.recomendacoes().isEmpty()) comRecomendacao++;
            for (ResultadoRecomendacao.Item i : r.recomendacoes()) {
                sugestoes++;
                potencial = potencial.add(i.preco());
                vezes.merge(i.produto(), 1, Integer::sum);
            }
        }
        int ativos = base.produtosAtivos().size();
        int[] acerto = testeRetroativo(base, pesos, versao);
        Map.Entry<String, Integer> top = vezes.entrySet().stream()
                .max(Map.Entry.<String, Integer>comparingByValue().thenComparing(Map.Entry.comparingByKey()))
                .orElse(null);

        long nConsultas = consultas.countByVersao(versao);
        long nComResultado = consultas.countByVersaoAndQuantidadeGreaterThan(versao, 0);

        return new Indicadores(
                versao, total, comRecomendacao, pct(comRecomendacao, total),
                ativos, vezes.size(), pct(vezes.size(), ativos),
                top == null ? null : top.getKey(), top == null ? 0 : pct(top.getValue(), sugestoes),
                total == 0 ? 0 : arred((double) sugestoes / total),
                total == 0 ? BigDecimal.ZERO : potencial.divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP),
                semHistorico, acerto[0] == 0 ? 0 : pct(acerto[1], acerto[0]), acerto[0], nConsultas, nComResultado,
                nConsultas == 0 ? null : pct(nComResultado, nConsultas));
    }

    /**
     * Para cada compra real (cliente com 2+ produtos), esconde essa compra e pergunta ao motor:
     * o produto escondido aparece no top 3? Devolve {testadas, acertos}.
     */
    int[] testeRetroativo(BaseHistorica base, Parametros.Pesos pesos, String versao) {
        int testadas = 0, acertos = 0;
        for (Cliente c : base.todosClientes()) {
            List<Long> comprados = base.produtosComprados(c.getId());
            if (comprados.size() < 2) continue;
            for (Long escondido : comprados) {
                if (base.produtosAtivos().stream().noneMatch(p -> p.getId().equals(escondido))) continue;
                ResultadoRecomendacao r = motor.recomendar(base.semCompra(c.getId(), escondido), c, pesos, versao);
                testadas++;
                if (r.recomendacoes().stream().anyMatch(i -> i.produtoId() == escondido)) acertos++;
            }
        }
        return new int[]{testadas, acertos};
    }

    private static double pct(long parte, long todo) {
        return todo == 0 ? 0 : arred(100.0 * parte / todo);
    }

    private static double arred(double x) {
        return Math.round(x * 10) / 10.0;
    }
}
