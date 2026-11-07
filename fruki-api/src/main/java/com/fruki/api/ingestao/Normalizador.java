package com.fruki.api.ingestao;

import com.fruki.api.dominio.Catalogo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;

/**
 * Limpeza e padronização dos dados que chegam pela ingestão.
 * Cada método devolve o valor padronizado ou null quando não dá para aproveitar.
 */
public final class Normalizador {
    private Normalizador() {}

    private static final Locale PT = Locale.of("pt", "BR");
    private static final Set<String> MINUSCULAS = Set.of("da", "das", "de", "do", "dos", "e");

    /** Tira espaços nas pontas, espaços repetidos e caracteres de controle. */
    public static String texto(String s) {
        if (s == null) return null;
        String t = s.replaceAll("\\p{Cntrl}", " ").replaceAll("\\s+", " ").trim();
        return t.isEmpty() ? null : t;
    }

    /** "  mercado   PINHEIRO da serra " -> "Mercado Pinheiro da Serra". */
    public static String nomeProprio(String s) {
        String t = texto(s);
        if (t == null) return null;
        String[] partes = t.toLowerCase(PT).split(" ");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < partes.length; i++) {
            String p = partes[i];
            if (i > 0) sb.append(' ');
            if (i > 0 && MINUSCULAS.contains(p)) sb.append(p);
            else sb.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1));
        }
        return sb.toString();
    }

    /** Minúsculas e sem acento, para comparar grafias diferentes. */
    public static String chave(String s) {
        String t = texto(s);
        if (t == null) return null;
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(PT);
    }

    /** " rs " -> "RS"; devolve null se não for uma UF brasileira. */
    public static String uf(String s) {
        String t = texto(s);
        if (t == null) return null;
        String u = t.toUpperCase(PT);
        return Catalogo.ESTADOS.containsKey(u) ? u : null;
    }

    /** "super mercado", "SUPERMERCADO", "Hipermercado" -> "Supermercado". */
    public static String segmento(String s) {
        String k = chave(s);
        if (k == null) return null;
        for (String seg : Catalogo.SEGMENTOS) if (chave(seg).equals(k)) return seg;
        return Catalogo.SINONIMOS_SEGMENTO.get(k);
    }

    /** "agua", "ÁGUA" -> "Água"; "refrigerante pet" -> "Refrigerante PET". */
    public static String familia(String s) {
        String k = chave(s);
        if (k == null) return null;
        for (String f : Catalogo.FAMILIAS) if (chave(f).equals(k)) return f;
        return null;
    }

    /** Arredonda para centavos. */
    public static BigDecimal preco(BigDecimal p) {
        return p == null ? null : p.setScale(2, RoundingMode.HALF_UP);
    }
}
