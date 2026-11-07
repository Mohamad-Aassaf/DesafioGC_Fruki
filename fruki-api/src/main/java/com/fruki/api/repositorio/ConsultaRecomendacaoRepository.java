package com.fruki.api.repositorio;

import com.fruki.api.dominio.ConsultaRecomendacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRecomendacaoRepository extends JpaRepository<ConsultaRecomendacao, Long> {
    long countByVersao(String versao);

    long countByVersaoAndQuantidadeGreaterThan(String versao, int quantidade);
}
