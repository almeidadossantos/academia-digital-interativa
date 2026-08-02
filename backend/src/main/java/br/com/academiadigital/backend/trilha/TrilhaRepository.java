package br.com.academiadigital.backend.trilha;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TrilhaRepository
        extends JpaRepository<Trilha, Long>,
        JpaSpecificationExecutor<Trilha> {

    boolean existsByTituloIgnoreCase(
            String titulo
    );

    boolean existsByTituloIgnoreCaseAndIdNot(
            String titulo,
            Long id
    );
}