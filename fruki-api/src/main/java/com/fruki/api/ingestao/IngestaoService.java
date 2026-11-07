package com.fruki.api.ingestao;

import com.fruki.api.dominio.Catalogo;
import com.fruki.api.dominio.Cliente;
import com.fruki.api.dominio.Compra;
import com.fruki.api.dominio.Produto;
import com.fruki.api.erro.ConflitoException;
import com.fruki.api.erro.EntradaInvalidaException;
import com.fruki.api.ingestao.Entradas.ClienteEntrada;
import com.fruki.api.ingestao.Entradas.CompraEntrada;
import com.fruki.api.ingestao.Entradas.LoteEntrada;
import com.fruki.api.ingestao.Entradas.ProdutoEntrada;
import com.fruki.api.ingestao.Respostas.Cadastro;
import com.fruki.api.ingestao.Respostas.ClienteResposta;
import com.fruki.api.ingestao.Respostas.CompraResposta;
import com.fruki.api.ingestao.Respostas.ItemRejeitado;
import com.fruki.api.ingestao.Respostas.ProdutoResposta;
import com.fruki.api.ingestao.Respostas.RelatorioLote;
import com.fruki.api.repositorio.ClienteRepository;
import com.fruki.api.repositorio.CompraRepository;
import com.fruki.api.repositorio.ProdutoRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Entrada de novos clientes, produtos e compras.
 * Fluxo de cada registro: validação de formato → limpeza/padronização → regras de negócio
 * (existência, duplicidade, datas) → gravação. O que foi ajustado na limpeza volta na resposta.
 */
@Service
public class IngestaoService {

    public static final String SEM_VENDEDOR = "Sem representante";
    /** Compras mais antigas que isso não ajudam a recomendação atual. */
    static final int MAX_ANOS_COMPRA = 2;

    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;
    private final CompraRepository compras;
    private final Validator validator;

    public IngestaoService(ClienteRepository clientes, ProdutoRepository produtos, CompraRepository compras,
                           Validator validator) {
        this.clientes = clientes;
        this.produtos = produtos;
        this.compras = compras;
        this.validator = validator;
    }

    public Cadastro<ClienteResposta> cadastrarCliente(ClienteEntrada e) {
        List<String> erros = new ArrayList<>();
        List<String> ajustes = new ArrayList<>();

        String nome = ajustar("nome", e.nome(), Normalizador.nomeProprio(e.nome()), ajustes);
        String cidade = ajustar("cidade", e.cidade(), Normalizador.nomeProprio(e.cidade()), ajustes);
        String uf = Normalizador.uf(e.uf());
        if (uf == null) erros.add("uf inválida: '" + e.uf() + "'. Use a sigla do estado, ex.: RS, SP, BA");
        else ajustar("uf", e.uf(), uf, ajustes);
        String segmento = Normalizador.segmento(e.segmento());
        if (segmento == null) {
            erros.add("segmento desconhecido: '" + e.segmento() + "'. Aceitos: " + String.join(", ", Catalogo.SEGMENTOS));
        } else {
            ajustar("segmento", e.segmento(), segmento, ajustes);
        }
        String vendedor = Normalizador.texto(e.vendedor());
        if (vendedor == null) {
            vendedor = SEM_VENDEDOR;
            ajustes.add("vendedor não informado: cliente fica como '" + SEM_VENDEDOR + "'");
        }
        if (!erros.isEmpty()) throw new EntradaInvalidaException("Cliente inválido", erros);

        if (clientes.existsByNomeIgnoreCaseAndCidadeIgnoreCaseAndUf(nome, cidade, uf)) {
            throw new ConflitoException("Já existe o cliente '" + nome + "' em " + cidade + "/" + uf);
        }
        Cliente salvo = clientes.save(new Cliente(nome, cidade, uf, segmento, vendedor));
        return new Cadastro<>(ClienteResposta.de(salvo, List.of()), ajustes);
    }

    public Cadastro<ProdutoResposta> cadastrarProduto(ProdutoEntrada e) {
        List<String> ajustes = new ArrayList<>();
        String nome = ajustar("nome", e.nome(), Normalizador.texto(e.nome()), ajustes);
        String familia = Normalizador.familia(e.familia());
        if (familia == null) {
            throw new EntradaInvalidaException("Produto inválido", List.of(
                    "familia desconhecida: '" + e.familia() + "'. Aceitas: " + String.join(", ", Catalogo.FAMILIAS)));
        }
        ajustar("familia", e.familia(), familia, ajustes);
        BigDecimal preco = Normalizador.preco(e.preco());
        if (preco.compareTo(e.preco()) != 0) ajustes.add("preco arredondado para " + preco);

        if (produtos.existsByNomeIgnoreCase(nome)) throw new ConflitoException("Já existe o produto '" + nome + "'");
        Produto salvo = produtos.save(new Produto(nome, familia, preco,
                Boolean.TRUE.equals(e.quente()), Boolean.TRUE.equals(e.saudavel())));
        return new Cadastro<>(ProdutoResposta.de(salvo), ajustes);
    }

    public Cadastro<CompraResposta> registrarCompra(CompraEntrada e) {
        List<String> erros = new ArrayList<>();
        List<String> ajustes = new ArrayList<>();

        Cliente cliente = clientes.findById(e.clienteId()).orElse(null);
        if (cliente == null) erros.add("clienteId " + e.clienteId() + " não existe");
        Produto produto = produtos.findById(e.produtoId()).orElse(null);
        if (produto == null) erros.add("produtoId " + e.produtoId() + " não existe");
        else if (!produto.isAtivo()) erros.add("produto '" + produto.getNome() + "' está inativo");

        LocalDate hoje = LocalDate.now();
        LocalDate data = e.data();
        if (data == null) {
            data = hoje;
            ajustes.add("data não informada: considerada hoje (" + hoje + ")");
        } else if (data.isAfter(hoje)) {
            erros.add("data no futuro: " + data);
        } else if (data.isBefore(hoje.minusYears(MAX_ANOS_COMPRA))) {
            erros.add("data muito antiga (" + data + "): aceitamos até " + MAX_ANOS_COMPRA + " anos atrás");
        }
        if (!erros.isEmpty()) throw new EntradaInvalidaException("Compra inválida", erros);

        if (compras.existsByClienteIdAndProdutoIdAndData(cliente.getId(), produto.getId(), data)) {
            throw new ConflitoException("Compra duplicada: " + cliente.getNome() + " já tem " + produto.getNome()
                    + " em " + data);
        }
        Compra salva = compras.save(new Compra(cliente, produto, e.quantidade(), data));
        return new Cadastro<>(CompraResposta.de(salva), ajustes);
    }

    /**
     * Processa uma carga em lote. Não é tudo-ou-nada: cada item válido é gravado e
     * cada item inválido volta no relatório com o motivo, sem interromper os demais.
     * Ordem: produtos, clientes e por último compras (que dependem dos dois).
     */
    public RelatorioLote processarLote(LoteEntrada lote) {
        List<ItemRejeitado> rejeitados = new ArrayList<>();
        List<String> ajustes = new ArrayList<>();
        int p = processar("produto", lote.produtos(), this::cadastrarProduto, rejeitados, ajustes);
        int c = processar("cliente", lote.clientes(), this::cadastrarCliente, rejeitados, ajustes);
        int k = processar("compra", lote.compras(), this::registrarCompra, rejeitados, ajustes);
        rejeitados.sort(Comparator.comparing(ItemRejeitado::tipo).thenComparingInt(ItemRejeitado::indice));
        return new RelatorioLote(c, p, k, rejeitados, ajustes);
    }

    private <E> int processar(String tipo, List<E> itens, Function<E, Cadastro<?>> cadastrar,
                              List<ItemRejeitado> rejeitados, List<String> ajustes) {
        if (itens == null) return 0;
        int aceitos = 0;
        for (int i = 0; i < itens.size(); i++) {
            E item = itens.get(i);
            if (item == null) {
                rejeitados.add(new ItemRejeitado(tipo, i, List.of("item vazio")));
                continue;
            }
            List<String> violacoes = validator.validate(item).stream()
                    .map(ConstraintViolation::getMessage).sorted().toList();
            if (!violacoes.isEmpty()) {
                rejeitados.add(new ItemRejeitado(tipo, i, violacoes));
                continue;
            }
            try {
                Cadastro<?> r = cadastrar.apply(item);
                final int indice = i;
                r.ajustes().forEach(a -> ajustes.add(tipo + " #" + indice + ": " + a));
                aceitos++;
            } catch (EntradaInvalidaException ex) {
                rejeitados.add(new ItemRejeitado(tipo, i, ex.getDetalhes()));
            } catch (ConflitoException ex) {
                rejeitados.add(new ItemRejeitado(tipo, i, List.of(ex.getMessage())));
            }
        }
        return aceitos;
    }

    /** Registra em "ajustes" quando a limpeza mudou o valor recebido. */
    private static String ajustar(String campo, String original, String limpo, List<String> ajustes) {
        if (limpo != null && !Objects.equals(original, limpo)) {
            ajustes.add(campo + ": '" + original + "' → '" + limpo + "'");
        }
        return limpo;
    }
}
