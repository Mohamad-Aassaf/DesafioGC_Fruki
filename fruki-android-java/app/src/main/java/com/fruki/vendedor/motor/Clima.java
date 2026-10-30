package com.fruki.vendedor.motor;

import android.graphics.Color;

import com.fruki.vendedor.data.Dados;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Previsão simulada de 14 dias por estado (temperatura, chuva e umidade). */
public final class Clima {
    private Clima() {}

    public static final class Dia {
        public final int temperatura;
        public final int chuva;
        public final int umidade;

        Dia(int temperatura, int chuva, int umidade) {
            this.temperatura = temperatura;
            this.chuva = chuva;
            this.umidade = umidade;
        }
    }

    private static final Map<String, List<Dia>> CACHE = new HashMap<>();

    public static List<Dia> previsao(String uf) {
        List<Dia> c = CACHE.get(uf);
        if (c != null) return c;
        int h = Dados.hash(uf);
        int[] base = Dados.BASE.get(Dados.REGIAO.get(uf));
        int t0 = base[0], u0 = base[1];
        List<Dia> dias = new ArrayList<>();
        for (int d = 0; d < 14; d++) {
            int r = (h * 7 + d * 13) % 55 + (t0 < 25 ? 15 : 0);
            int t = (int) Math.round(t0 + 3 * Math.sin((d + h) * .9) + (h % 3) - 1);
            int u = Math.min(98, (int) Math.round(u0 + r / 5.0 + 6 * Math.sin((d + h) * .7)));
            dias.add(new Dia(t, r, u));
        }
        CACHE.put(uf, dias);
        return dias;
    }

    public static int temperaturaMedia(String uf) {
        int s = 0;
        for (Dia d : previsao(uf)) s += d.temperatura;
        return Math.round(s / 14f);
    }

    public static int umidadeMedia(String uf) {
        int s = 0;
        for (Dia d : previsao(uf)) s += d.umidade;
        return Math.round(s / 14f);
    }

    /** "alta", "baixa" ou null quando a umidade é normal. */
    public static String classeUmidade(int u) {
        return u >= 75 ? "alta" : u <= 45 ? "baixa" : null;
    }

    public static String dicaUmidade(String classe) {
        if ("alta".equals(classe)) return "o suor evapora pouco, priorize água e bebidas sem açúcar.";
        if ("baixa".equals(classe)) return "ar seco desidrata, priorize água e chás.";
        return "";
    }

    /** Escala de cor do mapa: azul (frio) até laranja (quente). */
    public static int corTemperatura(int t) {
        float h = Math.max(8, Math.min(215, 215 - (t - 18) * 14));
        return hsl(h, .62f, .44f);
    }

    public static int hsl(float h, float s, float l) {
        float c = (1 - Math.abs(2 * l - 1)) * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = l - c / 2;
        float r, g, b;
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        return Color.rgb(Math.round((r + m) * 255), Math.round((g + m) * 255), Math.round((b + m) * 255));
    }
}
