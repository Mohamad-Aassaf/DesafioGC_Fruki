package com.fruki.vendedor.data;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/** Modelos simples usados pelo protótipo (dados simulados). */
public final class Modelos {
    private Modelos() {}

    public static final class Produto {
        public final int id;
        public final String nome;
        public final String familia;
        /** Vende mais no calor. */
        public final boolean quente;
        /** Opção de hidratação / sem açúcar. */
        public final boolean saudavel;

        public Produto(int id, String nome, String familia, boolean quente, boolean saudavel) {
            this.id = id;
            this.nome = nome;
            this.familia = familia;
            this.quente = quente;
            this.saudavel = saudavel;
        }

        /** Preço simulado por caixa. */
        public int preco() {
            return 20 + Dados.hash(nome) % 40;
        }
    }

    public static final class Familia {
        public final String nome;
        /** Volume em mil unidades nos últimos 12 meses. */
        public final int volume;
        public final LinkedHashMap<String, Integer> produtos = new LinkedHashMap<>();

        public Familia(String nome, int volume, Object... pares) {
            this.nome = nome;
            this.volume = volume;
            for (int i = 0; i < pares.length; i += 2) {
                produtos.put((String) pares[i], (Integer) pares[i + 1]);
            }
        }
    }

    public static final class Evento {
        public final LocalDate data;
        public final String nome;
        public final String abrangencia;
        /** Famílias que vendem mais nessa data. */
        public final List<String> familias;

        public Evento(String data, String nome, String abrangencia, String... familias) {
            this.data = LocalDate.parse(data);
            this.nome = nome;
            this.abrangencia = abrangencia;
            this.familias = Arrays.asList(familias);
        }
    }

    public static final class Cliente {
        public final String nome;
        public final String cidade;
        public final String uf;
        public final String segmento;
        /** Ids dos produtos que o cliente já compra. */
        public final List<Integer> mix;

        public Cliente(String nome, String cidade, String uf, String segmento, Integer... mix) {
            this.nome = nome;
            this.cidade = cidade;
            this.uf = uf;
            this.segmento = segmento;
            this.mix = Arrays.asList(mix);
        }
    }
}
