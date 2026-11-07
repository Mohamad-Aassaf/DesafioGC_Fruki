package com.fruki.api.clima;

import java.util.List;

/**
 * Previsão de 14 dias para uma UF.
 *
 * @param fonte "open-meteo" ou "simulado"
 * @param aviso preenchido quando a fonte configurada falhou e foi usada a estimativa
 */
public record PrevisaoClima(String uf, List<DiaClima> dias, String fonte, String aviso) {

    public int temperaturaMedia() {
        return (int) Math.round(dias.stream().mapToInt(DiaClima::temperatura).average().orElse(0));
    }

    public int umidadeMedia() {
        return (int) Math.round(dias.stream().mapToInt(DiaClima::umidade).average().orElse(0));
    }

    /** "alta" (≥ 75%), "baixa" (≤ 45%) ou null quando a umidade está normal. */
    public String classeUmidade() {
        int u = umidadeMedia();
        return u >= 75 ? "alta" : u <= 45 ? "baixa" : null;
    }
}
