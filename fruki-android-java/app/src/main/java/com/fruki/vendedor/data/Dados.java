package com.fruki.vendedor.data;

import com.fruki.vendedor.data.Modelos.Cliente;
import com.fruki.vendedor.data.Modelos.Evento;
import com.fruki.vendedor.data.Modelos.Familia;
import com.fruki.vendedor.data.Modelos.Produto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Dados simulados do protótipo: estados, produtos, vendas, eventos e clientes. */
public final class Dados {
    private Dados() {}

    /** Contorno simplificado dos estados: pares "longitude,latitude". */
    public static final Map<String, String> CONTORNOS = new LinkedHashMap<>();
    /** UF -> região (N, NE, CO, SE, S). */
    public static final Map<String, String> REGIAO = new HashMap<>();
    /** Região -> {temperatura base, umidade base}. */
    public static final Map<String, int[]> BASE = new HashMap<>();
    public static final List<Produto> PRODUTOS = new ArrayList<>();
    public static final Map<String, Familia> FAMILIAS = new LinkedHashMap<>();
    public static final List<Evento> EVENTOS = new ArrayList<>();
    public static final Map<String, List<Cliente>> VENDEDORES = new LinkedHashMap<>();

    static {
        CONTORNOS.put("RR", "-64.8,1.6 -64,3.8 -63.4,4.2 -61.5,5 -60.7,5.2 -60.2,5.25 -59.8,4.4 -60,3 -59.2,2.4 -58.5,1.3 -58.9,0.4 -60.5,-1.1 -62,0.3 -63.5,1");
        CONTORNOS.put("AP", "-54,2.3 -52.4,2.9 -51.6,4.4 -51,3 -50.4,1.7 -50,0.8 -50.3,0.1 -51,-0.5 -52.6,-1.2 -53.2,0.3");
        CONTORNOS.put("PA", "-54,2.3 -53.2,0.3 -52.6,-1.2 -51,-0.5 -50.3,0.1 -49.5,-0.2 -48.5,-0.8 -47.3,-0.6 -46.1,-1.1 -46.2,-3.2 -47.2,-4.2 -48.4,-5.4 -48.8,-7 -49.5,-8.5 -50.2,-9.3 -53.5,-8.9 -55.8,-7.8 -58.4,-7.3 -58.2,-4.5 -57.2,-2.3 -57.8,0 -58.5,1.3 -56.5,2");
        CONTORNOS.put("AM", "-73.8,-7.3 -72,-7.5 -70,-7.6 -68.5,-8.5 -66.7,-9.8 -63.5,-8.5 -60.4,-8.8 -58.4,-7.3 -58.2,-4.5 -57.2,-2.3 -57.8,0 -58.9,0.4 -60.5,-1.1 -62,0.3 -63.5,1 -64.8,1.6 -67,2 -69,1.1 -70,0.5 -69.5,-1.2 -70,-4.2");
        CONTORNOS.put("AC", "-73.8,-7.3 -72,-7.5 -70,-7.6 -68.5,-8.5 -66.7,-9.8 -67.5,-10.5 -68.7,-11.1 -70.5,-11 -72.2,-9.9 -73,-8.8");
        CONTORNOS.put("RO", "-66.7,-9.8 -63.5,-8.5 -60.4,-8.8 -60.3,-11.5 -60.4,-13.5 -62.5,-13 -64.5,-12.3 -65.4,-11.3 -65,-10");
        CONTORNOS.put("MT", "-58.4,-7.3 -55.8,-7.8 -53.5,-8.9 -50.2,-9.3 -50.6,-11.5 -50.5,-13 -52,-15 -53.2,-17.8 -55,-17.2 -57.5,-17.6 -58.2,-16.3 -60,-15.2 -60.4,-13.5 -60.3,-11.5 -60.4,-8.8");
        CONTORNOS.put("TO", "-48.4,-5.4 -48.8,-7 -49.5,-8.5 -50.2,-9.3 -50.6,-11.5 -50.5,-13 -48.5,-13.3 -46.5,-12.5 -46,-10.5 -46,-10 -47.4,-8 -47.7,-5.8");
        CONTORNOS.put("MA", "-46.1,-1.1 -45,-1.5 -44,-2.4 -42.5,-2.7 -41.8,-2.8 -42.8,-4 -43.5,-6 -44.2,-7 -45.4,-9 -46,-10 -47.4,-8 -47.7,-5.8 -48.4,-5.4 -47.2,-4.2 -46.2,-3.2");
        CONTORNOS.put("PI", "-41.8,-2.8 -41.2,-3 -41.4,-4.2 -40.5,-6 -40.8,-7.5 -41,-9 -41.4,-9.5 -43,-10.8 -45.8,-10.7 -45.4,-9 -44.2,-7 -43.5,-6 -42.8,-4");
        CONTORNOS.put("CE", "-41.2,-3 -39.5,-2.9 -38.5,-3.7 -37.3,-4.6 -38,-5.6 -38.6,-6.8 -39.2,-7.8 -40.5,-7.3 -40.5,-6 -41.4,-4.2");
        CONTORNOS.put("RN", "-37.3,-4.6 -35.5,-5.1 -35.1,-5.8 -35,-6.5 -36.7,-6.5 -38,-6.2 -38,-5.6");
        CONTORNOS.put("PB", "-35,-6.5 -34.8,-7.2 -35.2,-7.5 -37,-8.1 -38.6,-7.2 -38.6,-6.8 -38,-6.2 -36.7,-6.5");
        CONTORNOS.put("PE", "-34.9,-7.4 -34.9,-8.2 -35.3,-9.2 -36.5,-9.5 -37.8,-9.2 -38.8,-9 -40,-8.8 -41,-9 -40.8,-7.5 -40.5,-7.3 -39.2,-7.8 -38.6,-7.2 -37,-8.1 -35.2,-7.5");
        CONTORNOS.put("AL", "-35.3,-9.2 -35.9,-9.9 -36.4,-10.5 -37,-10 -37.8,-9.3 -37.8,-9.2 -36.5,-9.5");
        CONTORNOS.put("SE", "-36.4,-10.5 -37,-11 -37.4,-11.6 -38.3,-10.9 -38,-10 -37.8,-9.4 -37,-10");
        CONTORNOS.put("BA", "-46,-10 -45.8,-10.7 -43,-10.8 -41.4,-9.5 -40,-8.8 -38.8,-9 -37.8,-9.4 -38,-10 -38.3,-10.9 -37.4,-11.6 -38.5,-13 -39,-14.8 -39,-17 -39.7,-18.3 -39.9,-18.2 -40.5,-17.3 -41.5,-16.5 -42.5,-15.7 -43.8,-15 -44.2,-14.2 -46.4,-14.3 -46.5,-12.5 -46,-10.5");
        CONTORNOS.put("GO", "-50.5,-13 -48.5,-13.3 -46.5,-12.5 -46.4,-14.3 -46.2,-16 -47.5,-17.5 -48.9,-18.4 -50,-18.9 -51,-19 -52.5,-18.5 -53.2,-17.8 -52,-15");
        CONTORNOS.put("DF", "-48.2,-15.5 -47.3,-15.5 -47.3,-16.05 -48.2,-16.05");
        CONTORNOS.put("MG", "-51,-19 -51,-20 -50,-20 -48.2,-20 -47.2,-20.7 -46.8,-22 -45.5,-22.6 -44.6,-22.3 -43.5,-22 -42.5,-21.1 -41.8,-21 -41,-20.5 -40.8,-19.5 -39.9,-18.2 -40.5,-17.3 -41.5,-16.5 -42.5,-15.7 -43.8,-15 -44.2,-14.2 -46.4,-14.3 -46.2,-16 -47.5,-17.5 -48.9,-18.4 -50,-18.9");
        CONTORNOS.put("ES", "-39.7,-18.3 -39.9,-18.2 -40.8,-19.5 -41,-20.5 -41.8,-21 -41,-21.3 -40.5,-20.7 -40,-19.5");
        CONTORNOS.put("RJ", "-41,-21.3 -41.8,-21 -42.5,-21.1 -43.5,-22 -44.6,-22.3 -44.7,-23.2 -43.2,-23 -42,-22.9 -41,-22");
        CONTORNOS.put("SP", "-44.6,-22.3 -45.5,-22.6 -46.8,-22 -47.2,-20.7 -48.2,-20 -51,-20 -52,-21.8 -53,-22.6 -51.5,-22.7 -50,-23.5 -49.2,-24 -48.2,-24.8 -48,-25.3 -46.5,-24.2 -45,-23.5 -44.7,-23.2");
        CONTORNOS.put("MS", "-53.2,-17.8 -51,-19 -51,-20 -52,-21.8 -53,-22.6 -54.3,-24 -55,-23.8 -55.7,-22.2 -57.8,-22 -57.6,-19 -57.5,-17.6 -55,-17.2");
        CONTORNOS.put("PR", "-53,-22.6 -51.5,-22.7 -50,-23.5 -49.2,-24 -48.2,-24.8 -48,-25.3 -48.6,-26.1 -50,-26.4 -51.5,-26.1 -53.8,-25.9 -54.6,-25.5 -54.3,-24.1");
        CONTORNOS.put("SC", "-48.6,-26.1 -48.5,-27.6 -48.8,-28.5 -49.7,-29.3 -50.5,-29 -51.5,-28.4 -52.5,-27.5 -53.8,-27.1 -53.8,-25.9 -51.5,-26.1 -50,-26.4");
        CONTORNOS.put("RS", "-49.7,-29.3 -50.2,-30.4 -51.2,-31.4 -52.2,-32.2 -53.4,-33.7 -53.2,-32.8 -54.5,-31.6 -55.5,-30.9 -57.6,-30.2 -56,-28.6 -54.5,-27.3 -53.8,-27.1 -52.5,-27.5 -51.5,-28.4 -50.5,-29");

        regiao("N", "RR AP AM PA AC RO TO");
        regiao("NE", "MA PI CE RN BA PE PB SE AL");
        regiao("CO", "MT GO DF MS");
        regiao("SE", "MG SP RJ ES");
        regiao("S", "PR SC RS");
        BASE.put("N", new int[]{32, 85});
        BASE.put("NE", new int[]{31, 72});
        BASE.put("CO", new int[]{30, 48});
        BASE.put("SE", new int[]{28, 65});
        BASE.put("S", new int[]{23, 75});

        familia(new Familia("Refrigerante PET", 78, "Guaraná 2L", 32, "Cola 2L", 21, "Laranja 2L", 13, "Guaraná Zero 2L", 12));
        familia(new Familia("Refrigerante lata", 45, "Guaraná 350 ml", 19, "Cola 350 ml", 14, "Uva 350 ml", 12));
        familia(new Familia("Energético lata", 30, "Elev 473 ml", 18, "Elev Zero 473 ml", 12));
        familia(new Familia("Água", 38, "Sem gás 1,5 L", 22, "Com gás 500 ml", 16));
        familia(new Familia("Chás e sucos", 22, "Chá limão 1,5 L", 13, "Chá pêssego 1,5 L", 9));

        PRODUTOS.add(new Produto(1, "Guaraná 2L", "Refrigerante PET", true, false));
        PRODUTOS.add(new Produto(2, "Cola 2L", "Refrigerante PET", true, false));
        PRODUTOS.add(new Produto(3, "Guaraná 350 ml", "Refrigerante lata", true, false));
        PRODUTOS.add(new Produto(4, "Elev 473 ml", "Energético lata", false, false));
        PRODUTOS.add(new Produto(5, "Água sem gás 1,5 L", "Água", true, true));
        PRODUTOS.add(new Produto(6, "Chá limão 1,5 L", "Chás e sucos", true, true));
        PRODUTOS.add(new Produto(7, "Guaraná Zero 2L", "Refrigerante PET", false, true));
        PRODUTOS.add(new Produto(8, "Uva 350 ml", "Refrigerante lata", false, false));

        EVENTOS.add(new Evento("2026-11-02", "Finados", "Nacional"));
        EVENTOS.add(new Evento("2026-11-20", "Consciência Negra", "Nacional"));
        EVENTOS.add(new Evento("2026-11-27", "Black Friday", "Nacional", "Refrigerante PET", "Refrigerante lata"));
        EVENTOS.add(new Evento("2026-12-25", "Natal", "Nacional", "Refrigerante PET", "Refrigerante lata"));
        EVENTOS.add(new Evento("2026-12-31", "Ano Novo", "Nacional", "Energético lata", "Refrigerante lata", "Água"));
        EVENTOS.add(new Evento("2027-02-02", "Festa de Iemanjá", "Sul e Nordeste", "Água"));
        EVENTOS.add(new Evento("2027-02-09", "Carnaval", "Nacional", "Água", "Energético lata", "Refrigerante lata"));
        EVENTOS.add(new Evento("2027-03-28", "Páscoa", "Nacional", "Chás e sucos"));
        EVENTOS.add(new Evento("2027-05-09", "Dia das Mães", "Nacional", "Refrigerante PET", "Chás e sucos"));

        VENDEDORES.put("Rep. 33307 (RS)", Arrays.asList(
                new Cliente("Mercado Pinheiro", "Canoas", "RS", "Mercado", 1, 2, 5),
                new Cliente("Padaria Trigo Bom", "Porto Alegre", "RS", "Padaria", 1, 3),
                new Cliente("Supermercado Lajeado", "Lajeado", "RS", "Supermercado", 1, 2, 3, 4),
                new Cliente("Minimercado Sol", "Gravataí", "RS", "Mercadinho", 2)));
        VENDEDORES.put("Rep. 31402 (Nordeste)", Arrays.asList(
                new Cliente("Mercadinho Central", "Salvador", "BA", "Mercadinho", 1, 3),
                new Cliente("Atacado Recife Sul", "Recife", "PE", "Atacado", 1, 2, 4),
                new Cliente("Bar da Praça", "Fortaleza", "CE", "Bar", 3)));
        VENDEDORES.put("Rep. 30110 (Norte)", Arrays.asList(
                new Cliente("Mercado Rio Negro", "Manaus", "AM", "Mercado", 1, 2),
                new Cliente("Distribuidora Belém", "Belém", "PA", "Atacado", 1, 3, 5)));
    }

    private static void regiao(String r, String ufs) {
        for (String uf : ufs.split(" ")) REGIAO.put(uf, r);
    }

    private static void familia(Familia f) {
        FAMILIAS.put(f.nome, f);
    }

    /** Soma dos códigos dos caracteres: dá números estáveis para simular dados. */
    public static int hash(String s) {
        int a = 0;
        for (int i = 0; i < s.length(); i++) a += s.charAt(i);
        return a;
    }

    public static Produto produto(int id) {
        for (Produto p : PRODUTOS) if (p.id == id) return p;
        return null;
    }
}
