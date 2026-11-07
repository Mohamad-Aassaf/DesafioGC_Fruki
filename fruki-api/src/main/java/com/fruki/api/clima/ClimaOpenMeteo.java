package com.fruki.api.clima;

import com.fruki.api.dominio.Catalogo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Previsão real de 14 dias para a capital da UF, pela API pública Open-Meteo (sem chave). */
@Component
public class ClimaOpenMeteo implements ProvedorClima {

    private final RestClient http;

    public ClimaOpenMeteo(@Value("${fruki.clima.timeout-ms:3000}") int timeoutMs) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofMillis(timeoutMs));
        fabrica.setReadTimeout(Duration.ofMillis(timeoutMs));
        this.http = RestClient.builder()
                .baseUrl("https://api.open-meteo.com/v1/forecast")
                .requestFactory(fabrica)
                .build();
    }

    record Resposta(Diario daily) {}

    record Diario(List<String> time,
                  List<Double> temperature_2m_max,
                  List<Double> relative_humidity_2m_mean,
                  List<Integer> precipitation_probability_max) {}

    @Override
    public String nome() {
        return "open-meteo";
    }

    @Override
    public List<DiaClima> previsao(String uf) {
        Catalogo.Estado estado = Catalogo.ESTADOS.get(uf);
        if (estado == null) throw new IllegalArgumentException("UF desconhecida: " + uf);

        Resposta r = http.get()
                .uri(u -> u
                        .queryParam("latitude", estado.latitude())
                        .queryParam("longitude", estado.longitude())
                        .queryParam("daily", "temperature_2m_max,relative_humidity_2m_mean,precipitation_probability_max")
                        .queryParam("forecast_days", 14)
                        .queryParam("timezone", "America/Sao_Paulo")
                        .build())
                .retrieve()
                .body(Resposta.class);

        if (r == null || r.daily() == null || r.daily().time() == null || r.daily().time().isEmpty()) {
            throw new IllegalStateException("Open-Meteo devolveu uma resposta vazia");
        }
        Diario d = r.daily();
        List<DiaClima> dias = new ArrayList<>();
        for (int i = 0; i < d.time().size(); i++) {
            dias.add(new DiaClima(
                    LocalDate.parse(d.time().get(i)),
                    (int) Math.round(valor(d.temperature_2m_max(), i)),
                    (int) Math.round(valor(d.precipitation_probability_max(), i)),
                    (int) Math.round(valor(d.relative_humidity_2m_mean(), i))));
        }
        return dias;
    }

    private static double valor(List<? extends Number> lista, int i) {
        if (lista == null || i >= lista.size() || lista.get(i) == null) {
            throw new IllegalStateException("Open-Meteo devolveu dados incompletos");
        }
        return lista.get(i).doubleValue();
    }
}
