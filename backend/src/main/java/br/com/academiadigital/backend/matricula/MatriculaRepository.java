package br.com.academiadigital.backend.matricula;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MatriculaRepository
        extends JpaRepository<Matricula, Long>,
        JpaSpecificationExecutor<Matricula> {

    boolean existsByAlunoIdAndCursoId(
            Long alunoId,
            Long cursoId
    );
}