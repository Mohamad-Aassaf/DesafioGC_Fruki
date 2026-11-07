package com.fruki.api.motor;

import com.fruki.api.erro.EntradaInvalidaException;

import java.util.ArrayList;
import java.util.List;

/**
 * Pesos e limites do motor, recebidos como parâmetros opcionais da consulta
 * (ex.: ?bonusCalor=20&limite=3). Campos nulos usam o valor padrão.
 */
public record Parametros(
        Integer pesoHistorico,
        Integer bonusCalor,
        Integer limiteCalor,
        Integer bonusUmidade,
        Integer bonusEvento,
        Integer janelaEventos,
        Integer bonusCesta,
        Integer pontuacaoMinima,
        Integer limite,
        Integer maxPorFamilia) {

    /** Valores já resolvidos (sem nulos) que o motor usa. */
    public record Pesos(
            int pesoHistorico, int bonusCalor, int limiteCalor, int bonusUmidade, int bonusEvento,
            int janelaEventos, int bonusCesta, int pontuacaoMinima, int limite, int maxPorFamilia) {}

    public static final Pesos PADRAO = new Pesos(100, 15, 28, 10, 12, 60, 10, 20, 5, 2);

    public static Parametros vazios() {
        return new Parametros(null, null, null, null, null, null, null, null, null, null);
    }

    /** Aplica os padrões e valida as faixas. Lança 400 com todos os problemas encontrados. */
    public Pesos resolver() {
        List<String> erros = new ArrayList<>();
        Pesos p = new Pesos(
                faixa("pesoHistorico", pesoHistorico, PADRAO.pesoHistorico(), 0, 300, erros),
                faixa("bonusCalor", bonusCalor, PADRAO.bonusCalor(), 0, 50, erros),
                faixa("limiteCalor", limiteCalor, PADRAO.limiteCalor(), 10, 45, erros),
                faixa("bonusUmidade", bonusUmidade, PADRAO.bonusUmidade(), 0, 50, erros),
                faixa("bonusEvento", bonusEvento, PADRAO.bonusEvento(), 0, 50, erros),
                faixa("janelaEventos", janelaEventos, PADRAO.janelaEventos(), 1, 365, erros),
                faixa("bonusCesta", bonusCesta, PADRAO.bonusCesta(), 0, 50, erros),
                faixa("pontuacaoMinima", pontuacaoMinima, PADRAO.pontuacaoMinima(), 0, 200, erros),
                faixa("limite", limite, PADRAO.limite(), 1, 20, erros),
                faixa("maxPorFamilia", maxPorFamilia, PADRAO.maxPorFamilia(), 1, 20, erros));
        if (!erros.isEmpty()) throw new EntradaInvalidaException("Parâmetros do motor fora da faixa", erros);
        return p;
    }

    private static int faixa(String nome, Integer valor, int padrao, int min, int max, List<String> erros) {
        if (valor == null) return padrao;
        if (valor < min || valor > max) {
            erros.add(nome + " deve estar entre " + min + " e " + max + " (recebido: " + valor + ")");
        }
        return valor;
    }
}
