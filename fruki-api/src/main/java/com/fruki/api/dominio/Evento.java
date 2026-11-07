package com.fruki.api.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Feriado ou data comercial que puxa vendas de certas famílias de produto. */
@Entity
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, length = 60)
    private String nome;

    /** Regiões onde o evento vale, separadas por vírgula ("BR" = país todo; ex.: "S,NE"). */
    @Column(nullable = false, length = 40)
    private String regioes;

    /** Famílias de produto que o evento puxa, separadas por ";". Vazio = não puxa nenhuma. */
    @Column(nullable = false, length = 200)
    private String familias;

    protected Evento() {}

    public Evento(LocalDate data, String nome, String regioes, String... familias) {
        this.data = data;
        this.nome = nome;
        this.regioes = regioes;
        this.familias = String.join(";", familias);
    }

    public Long getId() { return id; }
    public LocalDate getData() { return data; }
    public String getNome() { return nome; }
    public String getRegioes() { return regioes; }

    public List<String> getFamilias() {
        return familias.isBlank() ? List.of() : Arrays.asList(familias.split(";"));
    }

    public boolean valeParaRegiao(String regiao) {
        return regioes.equals("BR") || Arrays.asList(regioes.split(",")).contains(regiao);
    }
}
