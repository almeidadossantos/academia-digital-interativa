package br.com.academiadigital.backend.progresso;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgressoAulaRepository
        extends JpaRepository<ProgressoAula, Long> {

    Optional<ProgressoAula>
            findByMatriculaIdAndAulaId(
                    Long matriculaId,
                    Long aulaId
            );

    List<ProgressoAula>
            findAllByMatriculaIdOrderByAulaOrdemAsc(
                    Long matriculaId
            );

    long countByMatriculaIdAndConcluidaTrue(
            Long matriculaId
    );
}