package com.fruki.api.clima;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entrega a previsão de uma UF usando a fonte configurada (fruki.clima.fonte).
 * Se a fonte externa falhar, usa o clima simulado e preenche um aviso: a recomendação
 * continua funcionando, só sem o clima real.
 */
@Service
public class ClimaService {

    private static final Logger log = LoggerFactory.getLogger(ClimaService.class);
    private static final Duration VALIDADE_CACHE = Duration.ofMinutes(30);

    private record Guardado(PrevisaoClima previsao, Instant em) {}

    private final ProvedorClima principal;
    private final ClimaSimulado simulado;
    private final Map<String, Guardado> cache = new ConcurrentHashMap<>();

    @Autowired
    public ClimaService(@Value("${fruki.clima.fonte:simulado}") String fonte,
                        ClimaSimulado simulado, ClimaOpenMeteo openMeteo) {
        this.simulado = simulado;
        this.principal = "open-meteo".equalsIgnoreCase(fonte.trim()) ? openMeteo : simulado;
    }

    /** Construtor para testes: permite injetar uma fonte que falha. */
    public ClimaService(ProvedorClima principal, ClimaSimulado simulado) {
        this.principal = principal;
        this.simulado = simulado;
    }

    public String fonteConfigurada() {
        return principal.nome();
    }

    public PrevisaoClima previsao(String uf) {
        Guardado g = cache.get(uf);
        if (g != null && g.em().plus(VALIDADE_CACHE).isAfter(Instant.now())) return g.previsao();

        PrevisaoClima p;
        try {
            p = new PrevisaoClima(uf, principal.previsao(uf), principal.nome(), null);
        } catch (Exception e) {
            log.warn("Falha ao consultar clima ({}) para {}: {}", principal.nome(), uf, e.toString());
            p = new PrevisaoClima(uf, simulado.previsao(uf), simulado.nome(),
                    "Falha ao consultar o clima em " + principal.nome() + "; usando estimativa simulada.");
            // Guarda o plano B por pouco tempo, para tentar a fonte real de novo logo.
            cache.put(uf, new Guardado(p, Instant.now().minus(VALIDADE_CACHE).plusSeconds(60)));
            return p;
        }
        cache.put(uf, new Guardado(p, Instant.now()));
        return p;
    }
}
