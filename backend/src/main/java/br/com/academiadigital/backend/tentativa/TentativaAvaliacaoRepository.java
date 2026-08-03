package br.com.academiadigital.backend.tentativa;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TentativaAvaliacaoRepository
        extends JpaRepository<TentativaAvaliacao, Long> {

    long countByMatriculaIdAndAvaliacaoId(
            Long matriculaId,
            Long avaliacaoId
    );

    Optional<TentativaAvaliacao>
            findFirstByMatriculaIdAndAvaliacaoIdAndStatusOrderByNumeroTentativaDesc(
                    Long matriculaId,
                    Long avaliacaoId,
                    StatusTentativa status
            );

    boolean existsByMatriculaIdAndAvaliacaoIdAndStatus(
            Long matriculaId,
            Long avaliacaoId,
            StatusTentativa status
    );

    Optional<TentativaAvaliacao> findByIdAndMatriculaAlunoId(
            Long tentativaId,
            Long alunoId
    );

    Page<TentativaAvaliacao> findAllByMatriculaAlunoId(
            Long alunoId,
            Pageable pageable
    );

    Page<TentativaAvaliacao> findAllByAvaliacaoId(
            Long avaliacaoId,
            Pageable pageable
    );
}
