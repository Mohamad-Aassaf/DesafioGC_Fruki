package com.fruki.api.motor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Resposta da consulta de recomendação: o ranking e tudo o que explica o ranking. */
public record ResultadoRecomendacao(
        String versao,
        ClienteResumo cliente,
        Contexto contexto,
        Parametros.Pesos parametros,
        List<Item> recomendacoes,
        int candidatosAvaliados,
        int descartadosPorPontuacao,
        List<String> avisos,
        LocalDateTime geradoEm) {

    public record ClienteResumo(long id, String nome, String cidade, String uf, String segmento,
                                String vendedor, int produtosNoHistorico) {}

    public record Contexto(String uf, String regiao, int temperaturaMedia, int umidadeMedia,
                           String classeUmidade, String fonteClima, List<EventoProximo> eventos) {}

    public record EventoProximo(String nome, LocalDate data, long emDias, List<String> familias) {}

    /**
     * Um produto recomendado.
     *
     * @param partes  pontos de cada critério (ex.: {"Histórico e segmento": 28, "Calor": 15})
     * @param motivos frases curtas que explicam cada parte, para o vendedor
     */
    public record Item(int posicao, long produtoId, String produto, String familia, BigDecimal preco,
                       int pontos, Map<String, Integer> partes, List<String> motivos) {}
}
