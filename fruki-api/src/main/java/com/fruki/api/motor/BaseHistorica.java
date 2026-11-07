package com.fruki.api.motor;

import com.fruki.api.dominio.Cliente;
import com.fruki.api.dominio.Compra;
import com.fruki.api.dominio.Evento;
import com.fruki.api.dominio.Produto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Retrato do banco usado em uma rodada do motor: clientes, produtos ativos, eventos futuros
 * e quais produtos cada cliente comprou dentro da janela de histórico.
 */
public final class BaseHistorica {

    final Map<Long, Cliente> clientes = new HashMap<>();
    final List<Produto> produtosAtivos;
    final List<Evento> eventos;
    /** Cliente -> produtos comprados na janela. */
    final Map<Long, Set<Long>> comprasPorCliente = new HashMap<>();

    BaseHistorica(List<Cliente> clientes, List<Produto> produtosAtivos, List<Evento> eventos, List<Compra> compras) {
        clientes.forEach(c -> this.clientes.put(c.getId(), c));
        this.produtosAtivos = produtosAtivos;
        this.eventos = eventos;
        for (Compra c : compras) {
            comprasPorCliente.computeIfAbsent(c.getCliente().getId(), k -> new HashSet<>()).add(c.getProduto().getId());
        }
    }

    private BaseHistorica(BaseHistorica origem) {
        this.clientes.putAll(origem.clientes);
        this.produtosAtivos = origem.produtosAtivos;
        this.eventos = origem.eventos;
        origem.comprasPorCliente.forEach((k, v) -> this.comprasPorCliente.put(k, new HashSet<>(v)));
    }

    /**
     * Cópia da base como se o cliente nunca tivesse comprado o produto.
     * Usada no teste retroativo: o motor consegue "adivinhar" uma compra que ele não viu?
     */
    public BaseHistorica semCompra(long clienteId, long produtoId) {
        BaseHistorica b = new BaseHistorica(this);
        Set<Long> s = b.comprasPorCliente.get(clienteId);
        if (s != null) {
            s.remove(produtoId);
            if (s.isEmpty()) b.comprasPorCliente.remove(clienteId);
        }
        return b;
    }

    Set<Long> comprados(long clienteId) {
        return comprasPorCliente.getOrDefault(clienteId, Set.of());
    }

    /** Clientes (exceto o informado) que têm histórico; se segmento != null, só os desse segmento. */
    List<Long> outrosComHistorico(long clienteId, String segmento) {
        return comprasPorCliente.keySet().stream()
                .filter(id -> id != clienteId)
                .filter(id -> segmento == null || segmento.equals(clientes.get(id).getSegmento()))
                .toList();
    }

    /** Fração dos clientes informados que compram o produto. */
    double taxaCompra(List<Long> grupo, long produtoId) {
        if (grupo.isEmpty()) return 0;
        long n = grupo.stream().filter(id -> comprados(id).contains(produtoId)).count();
        return (double) n / grupo.size();
    }

    /** Ids dos produtos que o cliente comprou na janela, em ordem. */
    public List<Long> produtosComprados(long clienteId) {
        return comprados(clienteId).stream().sorted().toList();
    }

    public List<Cliente> todosClientes() {
        return List.copyOf(clientes.values());
    }

    public List<Produto> produtosAtivos() {
        return produtosAtivos;
    }
}
