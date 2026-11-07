package com.fruki.api.repositorio;

import com.fruki.api.dominio.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findAllByOrderByVendedorAscNomeAsc();

    List<Cliente> findByVendedorOrderByNomeAsc(String vendedor);

    boolean existsByNomeIgnoreCaseAndCidadeIgnoreCaseAndUf(String nome, String cidade, String uf);
}
