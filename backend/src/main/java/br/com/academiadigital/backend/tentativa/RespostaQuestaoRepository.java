package br.com.academiadigital.backend.tentativa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RespostaQuestaoRepository
        extends JpaRepository<RespostaQuestao, Long> {

    Optional<RespostaQuestao> findByTentativaIdAndQuestaoId(
            Long tentativaId,
            Long questaoId
    );

    List<RespostaQuestao> findAllByTentativaIdOrderByQuestaoOrdemAsc(
            Long tentativaId
    );

    long countByTentativaId(Long tentativaId);
}
