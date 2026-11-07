package com.fruki.api.repositorio;

import com.fruki.api.dominio.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findAllByOrderByIdAsc();

    List<Produto> findByAtivoTrueOrderByIdAsc();

    boolean existsByNomeIgnoreCase(String nome);
}
