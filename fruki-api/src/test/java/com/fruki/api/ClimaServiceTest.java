package com.fruki.api;

import com.fruki.api.clima.ClimaService;
import com.fruki.api.clima.ClimaSimulado;
import com.fruki.api.clima.DiaClima;
import com.fruki.api.clima.PrevisaoClima;
import com.fruki.api.clima.ProvedorClima;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Falha de integração: se a fonte de clima externa cair, a recomendação segue com o clima simulado. */
class ClimaServiceTest {

    static class FonteForaDoAr implements ProvedorClima {
        @Override
        public String nome() {
            return "open-meteo";
        }

        @Override
        public List<DiaClima> previsao(String uf) throws Exception {
            throw new java.net.SocketTimeoutException("timeout simulado");
        }
    }

    @Test
    void fonteExternaFora_usaSimuladoEAvisa() {
        ClimaService clima = new ClimaService(new FonteForaDoAr(), new ClimaSimulado());
        PrevisaoClima p = clima.previsao("RS");
        assertThat(p.fonte()).isEqualTo("simulado");
        assertThat(p.aviso()).contains("Falha ao consultar o clima");
        assertThat(p.dias()).hasSize(14);
    }

    @Test
    void simuladoEhEstavelEPlausivel() {
        PrevisaoClima a = new PrevisaoClima("BA", new ClimaSimulado().previsao("BA"), "simulado", null);
        PrevisaoClima b = new PrevisaoClima("BA", new ClimaSimulado().previsao("BA"), "simulado", null);
        assertThat(a.temperaturaMedia()).isEqualTo(b.temperaturaMedia()).isBetween(20, 40);
        assertThat(a.umidadeMedia()).isBetween(30, 98);
    }
}
