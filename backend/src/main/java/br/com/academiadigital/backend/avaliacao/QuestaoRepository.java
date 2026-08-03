package br.com.academiadigital.backend.avaliacao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface QuestaoRepository
        extends JpaRepository<Questao, Long>,
                JpaSpecificationExecutor<Questao> {

    boolean existsByAvaliacaoIdAndOrdem(
            Long avaliacaoId,
            Integer ordem
    );

    boolean existsByAvaliacaoIdAndOrdemAndIdNot(
            Long avaliacaoId,
            Integer ordem,
            Long questaoId
    );

    List<Questao> findAllByAvaliacaoIdOrderByOrdemAsc(
            Long avaliacaoId
    );
}
