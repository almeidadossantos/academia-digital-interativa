package br.com.academiadigital.backend.aula;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AulaRepository
        extends JpaRepository<Aula, Long>,
                JpaSpecificationExecutor<Aula> {

    boolean existsByCursoIdAndOrdem(
            Long cursoId,
            Integer ordem
    );

    boolean existsByCursoIdAndOrdemAndIdNot(
            Long cursoId,
            Integer ordem,
            Long aulaId
    );

    List<Aula> findAllByCursoIdOrderByOrdemAsc(
            Long cursoId
    );
}
