package com.fruki.vendedor.motor;

import com.fruki.vendedor.data.Dados;
import com.fruki.vendedor.data.Modelos.Cliente;
import com.fruki.vendedor.data.Modelos.Evento;
import com.fruki.vendedor.data.Modelos.Produto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Motor de recomendação por regras e pontuação (scoring).
 * Cruza o histórico do segmento, o clima do estado do cliente e os próximos eventos
 * e devolve os produtos que o cliente ainda não compra, do mais para o menos indicado.
 */
public final class MotorRecomendacao {
    private MotorRecomendacao() {}

    public static final String HISTORICO = "Histórico e segmento";
    public static final String CLIMA = "Calor";
    public static final String UMIDADE = "Umidade";
    public static final String EVENTO = "Eventos";
    public static final String AJUSTE = "Ajuste";

    public static final class Recomendacao {
        public final Produto produto;
        public int pontos;
        /** Pontos de cada critério, na ordem em que foram somados. */
        public final LinkedHashMap<String, Integer> partes = new LinkedHashMap<>();
        public final List<String> motivos = new ArrayList<>();

        Recomendacao(Produto produto) {
            this.produto = produto;
        }

        void somar(String criterio, int valor, String motivo) {
            if (valor == 0) return;
            partes.put(criterio, valor);
            pontos += valor;
            motivos.add(motivo);
        }
    }

    public static Recomendacao pontuar(Cliente c, Produto p, Parametros pr) {
        Recomendacao r = new Recomendacao(p);

        int base = Dados.hash(c.nome + p.nome) % 25 + 30;
        r.somar(HISTORICO, Math.round(base * pr.pesoHistorico / 100f),
                "clientes do segmento " + c.segmento.toLowerCase() + " compram");

        int t = Clima.temperaturaMedia(c.uf);
        if (t >= pr.limiteCalor && p.quente) {
            r.somar(CLIMA, pr.bonusCalor, "calor (" + t + "°C)");
        }

        int u = Clima.umidadeMedia(c.uf);
        String classe = Clima.classeUmidade(u);
        if (p.saudavel && classe != null) {
            r.somar(UMIDADE, pr.bonusUmidade, "umidade " + classe + " (" + u + "%): hidratação");
        }
        if ("Energético lata".equals(p.familia) && "alta".equals(classe)) {
            r.somar(AJUSTE, -8, "calor úmido: energético menos indicado");
        }

        Evento e = proximoEvento(p.familia, pr.janelaEventos);
        if (e != null) {
            r.somar(EVENTO, pr.bonusEvento, e.nome + " em " + dias(e) + " d");
        }
        return r;
    }

    /** Ranking dos produtos que o cliente ainda não compra. */
    public static List<Recomendacao> ranking(Cliente c, Parametros pr) {
        List<Recomendacao> lista = new ArrayList<>();
        for (Produto p : Dados.PRODUTOS) {
            if (!c.mix.contains(p.id)) lista.add(pontuar(c, p, pr));
        }
        Collections.sort(lista, (a, b) -> b.pontos - a.pontos);
        return lista;
    }

    public static long dias(Evento e) {
        return ChronoUnit.DAYS.between(LocalDate.now(), e.data);
    }

    public static List<Evento> eventosFuturos() {
        List<Evento> l = new ArrayList<>();
        for (Evento e : Dados.EVENTOS) if (dias(e) >= 0) l.add(e);
        Collections.sort(l, (a, b) -> a.data.compareTo(b.data));
        return l;
    }

    public static Evento proximoEvento(String familia, int janela) {
        for (Evento e : eventosFuturos()) {
            if (e.familias.contains(familia) && dias(e) <= janela) return e;
        }
        return null;
    }
}
