package com.fruki.api.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

/** SKU do portfólio Fruki. */
@Entity
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @Column(nullable = false, length = 40)
    private String familia;

    /** Preço da caixa, em R$. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    /** Vende mais no calor. */
    private boolean quente;

    /** Opção de hidratação / sem açúcar. */
    private boolean saudavel;

    /** Produtos inativos não são recomendados nem aceitam compras novas. */
    private boolean ativo = true;

    protected Produto() {}

    public Produto(String nome, String familia, BigDecimal preco, boolean quente, boolean saudavel) {
        this.nome = nome;
        this.familia = familia;
        this.preco = preco;
        this.quente = quente;
        this.saudavel = saudavel;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getFamilia() { return familia; }
    public BigDecimal getPreco() { return preco; }
    public boolean isQuente() { return quente; }
    public boolean isSaudavel() { return saudavel; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}
