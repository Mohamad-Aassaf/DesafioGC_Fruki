package com.fruki.api.web;

import com.fruki.api.erro.NaoEncontradoException;
import com.fruki.api.ingestao.Entradas.ClienteEntrada;
import com.fruki.api.ingestao.Entradas.CompraEntrada;
import com.fruki.api.ingestao.Entradas.LoteEntrada;
import com.fruki.api.ingestao.Entradas.ProdutoEntrada;
import com.fruki.api.ingestao.IngestaoService;
import com.fruki.api.ingestao.Respostas.Cadastro;
import com.fruki.api.ingestao.Respostas.ClienteResposta;
import com.fruki.api.ingestao.Respostas.CompraResposta;
import com.fruki.api.ingestao.Respostas.ProdutoResposta;
import com.fruki.api.ingestao.Respostas.RelatorioLote;
import com.fruki.api.motor.BaseHistorica;
import com.fruki.api.motor.MotorRecomendacao;
import com.fruki.api.repositorio.ClienteRepository;
import com.fruki.api.repositorio.CompraRepository;
import com.fruki.api.repositorio.ProdutoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ingestão e consulta de clientes, produtos e compras. */
@RestController
@RequestMapping("/api")
public class CadastroController {

    private final IngestaoService ingestao;
    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;
    private final CompraRepository compras;
    private final MotorRecomendacao motor;

    public CadastroController(IngestaoService ingestao, ClienteRepository clientes, ProdutoRepository produtos,
                              CompraRepository compras, MotorRecomendacao motor) {
        this.ingestao = ingestao;
        this.clientes = clientes;
        this.produtos = produtos;
        this.compras = compras;
        this.motor = motor;
    }

    // ----- clientes -----

    @GetMapping("/clientes")
    public List<ClienteResposta> listarClientes(@RequestParam(required = false) String vendedor) {
        BaseHistorica base = motor.carregarBase();
        var lista = vendedor == null || vendedor.isBlank()
                ? clientes.findAllByOrderByVendedorAscNomeAsc()
                : clientes.findByVendedorOrderByNomeAsc(vendedor.trim());
        return lista.stream().map(c -> ClienteResposta.de(c, base.produtosComprados(c.getId()))).toList();
    }

    /** Clientes agrupados por representante (a tela do app começa por aqui). */
    @GetMapping("/vendedores")
    public List<Map<String, Object>> vendedores() {
        Map<String, List<ClienteResposta>> grupos = new LinkedHashMap<>();
        for (ClienteResposta c : listarClientes(null)) {
            grupos.computeIfAbsent(c.vendedor(), k -> new java.util.ArrayList<>()).add(c);
        }
        return grupos.entrySet().stream()
                .map(e -> Map.<String, Object>of("vendedor", e.getKey(), "clientes", e.getValue()))
                .toList();
    }

    @GetMapping("/clientes/{id}")
    public ClienteResposta cliente(@PathVariable long id) {
        var c = clientes.findById(id).orElseThrow(() -> new NaoEncontradoException("Cliente " + id + " não encontrado"));
        return ClienteResposta.de(c, motor.carregarBase().produtosComprados(id));
    }

    @PostMapping("/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    public Cadastro<ClienteResposta> novoCliente(@Valid @RequestBody ClienteEntrada entrada) {
        return ingestao.cadastrarCliente(entrada);
    }

    @GetMapping("/clientes/{id}/compras")
    public List<CompraResposta> comprasDoCliente(@PathVariable long id) {
        if (!clientes.existsById(id)) throw new NaoEncontradoException("Cliente " + id + " não encontrado");
        return compras.findByClienteIdOrderByDataDesc(id).stream().map(CompraResposta::de).toList();
    }

    // ----- produtos -----

    @GetMapping("/produtos")
    public List<ProdutoResposta> listarProdutos() {
        return produtos.findAllByOrderByIdAsc().stream().map(ProdutoResposta::de).toList();
    }

    @PostMapping("/produtos")
    @ResponseStatus(HttpStatus.CREATED)
    public Cadastro<ProdutoResposta> novoProduto(@Valid @RequestBody ProdutoEntrada entrada) {
        return ingestao.cadastrarProduto(entrada);
    }

    // ----- compras -----

    @PostMapping("/compras")
    @ResponseStatus(HttpStatus.CREATED)
    public Cadastro<CompraResposta> novaCompra(@Valid @RequestBody CompraEntrada entrada) {
        return ingestao.registrarCompra(entrada);
    }

    // ----- lote -----

    @PostMapping("/ingestao/lote")
    public RelatorioLote lote(@RequestBody LoteEntrada lote) {
        return ingestao.processarLote(lote);
    }
}
