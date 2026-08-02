package br.com.academiadigital.backend.avaliacao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AvaliacaoRepository
        extends JpaRepository<Avaliacao, Long>,
                JpaSpecificationExecutor<Avaliacao> {

    boolean existsByCursoIdAndOrdem(
            Long cursoId,
            Integer ordem
    );

    boolean existsByCursoIdAndOrdemAndIdNot(
            Long cursoId,
            Integer ordem,
            Long avaliacaoId
    );

    List<Avaliacao> findAllByCursoIdOrderByOrdemAsc(
            Long cursoId
    );
}
