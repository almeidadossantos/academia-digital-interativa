package br.com.academiadigital.backend.curso;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CursoRepository
        extends JpaRepository<Curso, Long>,
                JpaSpecificationExecutor<Curso> {

    boolean existsByTituloIgnoreCase(String titulo);
}