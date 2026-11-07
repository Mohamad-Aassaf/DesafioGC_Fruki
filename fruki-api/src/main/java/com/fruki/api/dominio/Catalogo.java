package com.fruki.api.dominio;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Listas de referência usadas para validar e normalizar dados de entrada. */
public final class Catalogo {
    private Catalogo() {}

    /** Estado: região e coordenadas da capital (usadas na previsão do tempo real). */
    public record Estado(String uf, String regiao, String capital, double latitude, double longitude) {}

    public static final Map<String, Estado> ESTADOS = new LinkedHashMap<>();

    static {
        estado("AC", "N", "Rio Branco", -9.97, -67.81);
        estado("AL", "NE", "Maceió", -9.67, -35.74);
        estado("AP", "N", "Macapá", 0.03, -51.07);
        estado("AM", "N", "Manaus", -3.12, -60.02);
        estado("BA", "NE", "Salvador", -12.97, -38.50);
        estado("CE", "NE", "Fortaleza", -3.73, -38.53);
        estado("DF", "CO", "Brasília", -15.79, -47.88);
        estado("ES", "SE", "Vitória", -20.32, -40.34);
        estado("GO", "CO", "Goiânia", -16.68, -49.25);
        estado("MA", "NE", "São Luís", -2.53, -44.30);
        estado("MT", "CO", "Cuiabá", -15.60, -56.10);
        estado("MS", "CO", "Campo Grande", -20.47, -54.62);
        estado("MG", "SE", "Belo Horizonte", -19.92, -43.94);
        estado("PA", "N", "Belém", -1.46, -48.49);
        estado("PB", "NE", "João Pessoa", -7.12, -34.86);
        estado("PR", "S", "Curitiba", -25.43, -49.27);
        estado("PE", "NE", "Recife", -8.05, -34.88);
        estado("PI", "NE", "Teresina", -5.09, -42.80);
        estado("RJ", "SE", "Rio de Janeiro", -22.91, -43.17);
        estado("RN", "NE", "Natal", -5.79, -35.21);
        estado("RS", "S", "Porto Alegre", -30.03, -51.23);
        estado("RO", "N", "Porto Velho", -8.76, -63.90);
        estado("RR", "N", "Boa Vista", 2.82, -60.67);
        estado("SC", "S", "Florianópolis", -27.59, -48.55);
        estado("SP", "SE", "São Paulo", -23.55, -46.63);
        estado("SE", "NE", "Aracaju", -10.91, -37.07);
        estado("TO", "N", "Palmas", -10.18, -48.33);
    }

    private static void estado(String uf, String regiao, String capital, double lat, double lon) {
        ESTADOS.put(uf, new Estado(uf, regiao, capital, lat, lon));
    }

    public static String regiao(String uf) {
        Estado e = ESTADOS.get(uf);
        return e == null ? "S" : e.regiao();
    }

    /** Região -> {temperatura base, umidade base} usadas pelo clima simulado. */
    public static final Map<String, int[]> CLIMA_BASE = Map.of(
            "N", new int[]{32, 85},
            "NE", new int[]{31, 72},
            "CO", new int[]{30, 48},
            "SE", new int[]{28, 65},
            "S", new int[]{23, 75});

    public static final List<String> FAMILIAS = List.of(
            "Refrigerante PET", "Refrigerante lata", "Energético lata", "Água", "Chás e sucos");

    public static final List<String> SEGMENTOS = List.of(
            "Mercado", "Supermercado", "Mercadinho", "Padaria", "Bar", "Lanchonete",
            "Restaurante", "Atacado", "Conveniência");

    /** Grafias comuns (sem acento, minúsculas) -> segmento padrão. */
    public static final Map<String, String> SINONIMOS_SEGMENTO = Map.ofEntries(
            Map.entry("minimercado", "Mercadinho"),
            Map.entry("mini mercado", "Mercadinho"),
            Map.entry("mercearia", "Mercadinho"),
            Map.entry("armazem", "Mercadinho"),
            Map.entry("super mercado", "Supermercado"),
            Map.entry("hipermercado", "Supermercado"),
            Map.entry("distribuidora", "Atacado"),
            Map.entry("atacadista", "Atacado"),
            Map.entry("atacarejo", "Atacado"),
            Map.entry("boteco", "Bar"),
            Map.entry("botequim", "Bar"),
            Map.entry("panificadora", "Padaria"),
            Map.entry("confeitaria", "Padaria"),
            Map.entry("loja de conveniencia", "Conveniência"),
            Map.entry("posto", "Conveniência"),
            Map.entry("lancheria", "Lanchonete"),
            Map.entry("cafeteria", "Lanchonete"));

    /** Soma dos códigos dos caracteres: o mesmo "hash" do app, para preços e dados estáveis. */
    public static int hash(String s) {
        int a = 0;
        for (int i = 0; i < s.length(); i++) a += s.charAt(i);
        return a;
    }
}
