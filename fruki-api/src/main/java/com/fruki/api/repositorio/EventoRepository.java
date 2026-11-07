package com.fruki.api.repositorio;

import com.fruki.api.dominio.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {
    List<Evento> findByDataGreaterThanEqualOrderByDataAsc(LocalDate inicio);
}
