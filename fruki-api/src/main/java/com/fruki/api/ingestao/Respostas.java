package com.fruki.api.ingestao;

import com.fruki.api.dominio.Cliente;
import com.fruki.api.dominio.Compra;
import com.fruki.api.dominio.Produto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Formatos de saída (JSON). As entidades do banco não são expostas diretamente. */
public final class Respostas {
    private Respostas() {}

    /** @param produtosComprados ids dos produtos comprados na janela de histórico */
    public record ClienteResposta(long id, String nome, String cidade, String uf, String segmento, String vendedor,
                                  List<Long> produtosComprados) {
        public static ClienteResposta de(Cliente c, List<Long> comprados) {
            return new ClienteResposta(c.getId(), c.getNome(), c.getCidade(), c.getUf(), c.getSegmento(),
                    c.getVendedor(), comprados);
        }
    }

    public record ProdutoResposta(long id, String nome, String familia, BigDecimal preco, boolean quente,
                                  boolean saudavel, boolean ativo) {
        public static ProdutoResposta de(Produto p) {
            return new ProdutoResposta(p.getId(), p.getNome(), p.getFamilia(), p.getPreco(), p.isQuente(),
                    p.isSaudavel(), p.isAtivo());
        }
    }

    public record CompraResposta(long id, long clienteId, String cliente, long produtoId, String produto,
                                 int quantidade, LocalDate data) {
        public static CompraResposta de(Compra c) {
            return new CompraResposta(c.getId(), c.getCliente().getId(), c.getCliente().getNome(),
                    c.getProduto().getId(), c.getProduto().getNome(), c.getQuantidade(), c.getData());
        }
    }

    /** Resposta de um cadastro: o registro já limpo e o que foi ajustado na limpeza. */
    public record Cadastro<T>(T registro, List<String> ajustes) {}

    public record ItemRejeitado(String tipo, int indice, List<String> erros) {}

    public record RelatorioLote(int clientesAceitos, int produtosAceitos, int comprasAceitas,
                                List<ItemRejeitado> rejeitados, List<String> ajustes) {}
}
