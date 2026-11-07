package com.fruki.api.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

/** Ponto de venda atendido por um representante (mercado, padaria, bar...). */
@Entity
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 80)
    private String cidade;

    @Column(nullable = false, length = 2)
    private String uf;

    @Column(nullable = false, length = 40)
    private String segmento;

    @Column(nullable = false, length = 60)
    private String vendedor;

    @Column(nullable = false)
    private LocalDateTime cadastradoEm = LocalDateTime.now();

    protected Cliente() {}

    public Cliente(String nome, String cidade, String uf, String segmento, String vendedor) {
        this.nome = nome;
        this.cidade = cidade;
        this.uf = uf;
        this.segmento = segmento;
        this.vendedor = vendedor;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCidade() { return cidade; }
    public String getUf() { return uf; }
    public String getSegmento() { return segmento; }
    public String getVendedor() { return vendedor; }
    public LocalDateTime getCadastradoEm() { return cadastradoEm; }
}
