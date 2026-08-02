package br.com.academiadigital.backend.trilha;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TrilhaCursoRepository
        extends JpaRepository<TrilhaCurso, Long> {

    List<TrilhaCurso> findAllByTrilhaIdOrderByOrdemAsc(
            Long trilhaId
    );

    Optional<TrilhaCurso> findByTrilhaIdAndCursoId(
            Long trilhaId,
            Long cursoId
    );

    boolean existsByTrilhaIdAndCursoId(
            Long trilhaId,
            Long cursoId
    );

    boolean existsByTrilhaIdAndOrdem(
            Long trilhaId,
            Integer ordem
    );

    boolean existsByTrilhaIdAndOrdemAndIdNot(
            Long trilhaId,
            Integer ordem,
            Long id
    );

    void deleteAllByTrilhaId(
            Long trilhaId
    );
}