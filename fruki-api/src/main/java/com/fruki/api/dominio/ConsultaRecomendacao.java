package com.fruki.api.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

/** Registro de cada consulta de recomendação: base para o indicador "taxa de recomendação gerada". */
@Entity
public class ConsultaRecomendacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false, length = 4)
    private String versao;

    /** Quantos produtos foram recomendados (0 = consulta sem recomendação). */
    private int quantidade;

    /** Ids dos produtos recomendados, na ordem do ranking (ex.: "5,6,12"). */
    @Column(length = 200)
    private String produtos;

    @Column(nullable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();

    protected ConsultaRecomendacao() {}

    public ConsultaRecomendacao(Long clienteId, String versao, int quantidade, String produtos) {
        this.clienteId = clienteId;
        this.versao = versao;
        this.quantidade = quantidade;
        this.produtos = produtos;
    }

    public Long getId() { return id; }
    public Long getClienteId() { return clienteId; }
    public String getVersao() { return versao; }
    public int getQuantidade() { return quantidade; }
    public String getProdutos() { return produtos; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
}
