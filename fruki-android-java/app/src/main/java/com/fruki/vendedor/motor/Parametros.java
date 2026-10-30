package com.fruki.vendedor.motor;

/** Pesos e limites do motor. O vendedor ajusta na tela "Parâmetros do motor". */
public final class Parametros {
    /** Peso do histórico e do segmento, em % (100 = normal). */
    public int pesoHistorico;
    /** Pontos extras para produtos de calor quando a média passa do limite. */
    public int bonusCalor;
    /** Temperatura média (°C) a partir da qual vale o bônus de calor. */
    public int limiteCalor;
    /** Pontos extras para hidratação quando a umidade está alta ou baixa. */
    public int bonusUmidade;
    /** Pontos extras quando um evento da família está perto. */
    public int bonusEvento;
    /** Quantos dias à frente olhar os eventos. */
    public int janelaEventos;
    /** Meta semanal em itens novos por cliente (R$). */
    public int metaPorCliente;

    public Parametros() {
        restaurar();
    }

    public void restaurar() {
        pesoHistorico = 100;
        bonusCalor = 15;
        limiteCalor = 28;
        bonusUmidade = 10;
        bonusEvento = 12;
        janelaEventos = 60;
        metaPorCliente = 150;
    }
}
