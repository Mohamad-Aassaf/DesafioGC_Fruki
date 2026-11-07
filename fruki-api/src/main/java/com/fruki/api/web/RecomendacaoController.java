package com.fruki.api.web;

import com.fruki.api.clima.ClimaService;
import com.fruki.api.clima.PrevisaoClima;
import com.fruki.api.config.DadosIniciais;
import com.fruki.api.erro.EntradaInvalidaException;
import com.fruki.api.indicadores.IndicadoresService;
import com.fruki.api.ingestao.Normalizador;
import com.fruki.api.motor.MotorRecomendacao;
import com.fruki.api.motor.Parametros;
import com.fruki.api.motor.ResultadoRecomendacao;
import com.fruki.api.repositorio.ClienteRepository;
import com.fruki.api.repositorio.CompraRepository;
import com.fruki.api.repositorio.EventoRepository;
import com.fruki.api.repositorio.ProdutoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consulta de recomendações, indicadores e contexto (clima, eventos). */
@RestController
@RequestMapping("/api")
public class RecomendacaoController {

    private final MotorRecomendacao motor;
    private final IndicadoresService indicadores;
    private final ClimaService clima;
    private final EventoRepository eventos;
    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;
    private final CompraRepository compras;
    private final DadosIniciais dadosIniciais;

    public RecomendacaoController(MotorRecomendacao motor, IndicadoresService indicadores, ClimaService clima,
                                  EventoRepository eventos, ClienteRepository clientes, ProdutoRepository produtos,
                                  CompraRepository compras, DadosIniciais dadosIniciais) {
        this.motor = motor;
        this.indicadores = indicadores;
        this.clima = clima;
        this.eventos = eventos;
        this.clientes = clientes;
        this.produtos = produtos;
        this.compras = compras;
        this.dadosIniciais = dadosIniciais;
    }

    /**
     * Ranking de produtos para o cliente.
     * Parâmetros opcionais: versao (v2 | v1) e os pesos do motor (pesoHistorico, bonusCalor, limiteCalor,
     * bonusUmidade, bonusEvento, janelaEventos, bonusCesta, pontuacaoMinima, limite, maxPorFamilia).
     */
    @GetMapping("/clientes/{id}/recomendacoes")
    public ResultadoRecomendacao recomendacoes(@PathVariable long id,
                                               @RequestParam(required = false) String versao,
                                               @ModelAttribute Parametros parametros) {
        return motor.recomendar(id, parametros == null ? Parametros.vazios() : parametros, versao);
    }

    @GetMapping("/indicadores")
    public IndicadoresService.Comparativo indicadores() {
        return indicadores.comparar();
    }

    @GetMapping("/eventos")
    public List<Map<String, Object>> eventos() {
        LocalDate hoje = LocalDate.now();
        return eventos.findByDataGreaterThanEqualOrderByDataAsc(hoje).stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("nome", e.getNome());
            m.put("data", e.getData());
            m.put("emDias", java.time.temporal.ChronoUnit.DAYS.between(hoje, e.getData()));
            m.put("regioes", e.getRegioes());
            m.put("familias", e.getFamilias());
            return m;
        }).toList();
    }

    @GetMapping("/clima/{uf}")
    public Map<String, Object> clima(@PathVariable String uf) {
        String u = Normalizador.uf(uf);
        if (u == null) throw new EntradaInvalidaException("uf inválida: '" + uf + "'");
        PrevisaoClima p = clima.previsao(u);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("uf", u);
        m.put("fonte", p.fonte());
        m.put("aviso", p.aviso());
        m.put("temperaturaMedia", p.temperaturaMedia());
        m.put("umidadeMedia", p.umidadeMedia());
        m.put("classeUmidade", p.classeUmidade());
        m.put("dias", p.dias());
        return m;
    }

    /** Verificação rápida: o app usa para saber se a API está no ar. */
    @GetMapping("/status")
    public Map<String, Object> status() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("status", "ok");
        m.put("versaoMotor", "v2");
        m.put("fonteClima", clima.fonteConfigurada());
        m.put("clientes", clientes.count());
        m.put("produtos", produtos.count());
        m.put("compras", compras.count());
        return m;
    }

    /** Volta o banco para a base de demonstração (apaga o que foi ingerido). */
    @PostMapping("/admin/restaurar-dados")
    public Map<String, Object> restaurar() {
        dadosIniciais.restaurar();
        return status();
    }
}
