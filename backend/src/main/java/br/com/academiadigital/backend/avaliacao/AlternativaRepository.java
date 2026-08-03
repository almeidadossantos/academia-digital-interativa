package br.com.academiadigital.backend.avaliacao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AlternativaRepository
        extends JpaRepository<Alternativa, Long> {

    boolean existsByQuestaoIdAndOrdem(
            Long questaoId,
            Integer ordem
    );

    boolean existsByQuestaoIdAndOrdemAndIdNot(
            Long questaoId,
            Integer ordem,
            Long alternativaId
    );

    List<Alternativa> findAllByQuestaoIdOrderByOrdemAsc(
            Long questaoId
    );
}
