package com.fruki.api.clima;

import java.time.LocalDate;

/** Previsão de um dia: temperatura máxima (°C), chance de chuva (%) e umidade média (%). */
public record DiaClima(LocalDate data, int temperatura, int chuva, int umidade) {}
