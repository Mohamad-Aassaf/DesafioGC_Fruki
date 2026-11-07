package com.fruki.api.repositorio;

import com.fruki.api.dominio.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByDataGreaterThanEqual(LocalDate inicio);

    List<Compra> findByClienteIdOrderByDataDesc(Long clienteId);

    boolean existsByClienteIdAndProdutoIdAndData(Long clienteId, Long produtoId, LocalDate data);
}
