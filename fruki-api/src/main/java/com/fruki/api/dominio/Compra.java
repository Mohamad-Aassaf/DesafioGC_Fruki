package com.fruki.api.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Uma compra (pedido de um SKU) feita por um cliente. É o histórico que alimenta o motor. */
@Entity
@Table(indexes = @Index(name = "idx_compra_data", columnList = "data"))
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    /** Quantidade de caixas. */
    @Column(nullable = false)
    private int quantidade;

    @Column(nullable = false)
    private LocalDate data;

    protected Compra() {}

    public Compra(Cliente cliente, Produto produto, int quantidade, LocalDate data) {
        this.cliente = cliente;
        this.produto = produto;
        this.quantidade = quantidade;
        this.data = data;
    }

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Produto getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public LocalDate getData() { return data; }
}
