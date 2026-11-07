package com.fruki.api.clima;

import com.fruki.api.dominio.Catalogo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Previsão estimada pela região da UF (mesma fórmula do app). Não depende de rede,
 * então serve de padrão para a demonstração e de plano B quando a fonte real falha.
 */
@Component
public class ClimaSimulado implements ProvedorClima {

    @Override
    public String nome() {
        return "simulado";
    }

    @Override
    public List<DiaClima> previsao(String uf) {
        int h = Catalogo.hash(uf);
        int[] base = Catalogo.CLIMA_BASE.get(Catalogo.regiao(uf));
        int t0 = base[0], u0 = base[1];
        LocalDate hoje = LocalDate.now();
        List<DiaClima> dias = new ArrayList<>();
        for (int d = 0; d < 14; d++) {
            int chuva = ((h * 7 + d * 13) % 55) + (t0 < 25 ? 15 : 0);
            int temperatura = (int) Math.round(t0 + 3 * Math.sin((d + h) * 0.9) + (h % 3) - 1);
            int umidade = (int) Math.min(98, Math.round(u0 + chuva / 5.0 + 6 * Math.sin((d + h) * 0.7)));
            dias.add(new DiaClima(hoje.plusDays(d), temperatura, chuva, umidade));
        }
        return dias;
    }
}
