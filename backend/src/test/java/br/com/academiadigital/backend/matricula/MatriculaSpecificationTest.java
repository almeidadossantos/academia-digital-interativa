package br.com.academiadigital.backend.matricula;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class MatriculaSpecificationTest {

    @Mock
    private Root<Matricula> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> alunoPath;

    @Mock
    private Path<Long> alunoIdPath;

    @Mock
    private Path<Object> cursoPath;

    @Mock
    private Path<Long> cursoIdPath;

    @Mock
    private Path<StatusMatricula> statusPath;

    @Mock
    private Predicate alunoPredicate;

    @Mock
    private Predicate cursoPredicate;

    @Mock
    private Predicate statusPredicate;

    @Mock
    private Predicate resultadoPredicate;

    @BeforeEach
    void configurarResultadoDaCombinacao() {
        when(criteriaBuilder.and(any(Predicate[].class)))
                .thenReturn(resultadoPredicate);
    }

    @Test
    void deveRetornarConjuncaoQuandoNaoHouverFiltros() {
        Specification<Matricula> specification =
                MatriculaSpecification.comFiltros(
                        null,
                        null,
                        null
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(criteriaBuilder).and();
        verifyNoInteractions(root);
    }

    @Test
    void deveFiltrarPorAluno() {
        when(root.<Object>get("aluno"))
                .thenReturn(alunoPath);

        when(alunoPath.<Long>get("id"))
                .thenReturn(alunoIdPath);

        when(criteriaBuilder.equal(
                alunoIdPath,
                1L
        )).thenReturn(alunoPredicate);

        Specification<Matricula> specification =
                MatriculaSpecification.comFiltros(
                        1L,
                        null,
                        null
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(criteriaBuilder).and(alunoPredicate);
    }

    @Test
    void deveFiltrarPorCurso() {
        when(root.<Object>get("curso"))
                .thenReturn(cursoPath);

        when(cursoPath.<Long>get("id"))
                .thenReturn(cursoIdPath);

        when(criteriaBuilder.equal(
                cursoIdPath,
                2L
        )).thenReturn(cursoPredicate);

        Specification<Matricula> specification =
                MatriculaSpecification.comFiltros(
                        null,
                        2L,
                        null
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(criteriaBuilder).and(cursoPredicate);
    }

    @Test
    void deveFiltrarPorStatus() {
        when(root.<StatusMatricula>get("status"))
                .thenReturn(statusPath);

        when(criteriaBuilder.equal(
                statusPath,
                StatusMatricula.ATIVA
        )).thenReturn(statusPredicate);

        Specification<Matricula> specification =
                MatriculaSpecification.comFiltros(
                        null,
                        null,
                        StatusMatricula.ATIVA
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(criteriaBuilder).and(statusPredicate);
    }

    @Test
    void deveCombinarAlunoCursoEStatus() {
        when(root.<Object>get("aluno"))
                .thenReturn(alunoPath);

        when(alunoPath.<Long>get("id"))
                .thenReturn(alunoIdPath);

        when(criteriaBuilder.equal(
                alunoIdPath,
                1L
        )).thenReturn(alunoPredicate);

        when(root.<Object>get("curso"))
                .thenReturn(cursoPath);

        when(cursoPath.<Long>get("id"))
                .thenReturn(cursoIdPath);

        when(criteriaBuilder.equal(
                cursoIdPath,
                2L
        )).thenReturn(cursoPredicate);

        when(root.<StatusMatricula>get("status"))
                .thenReturn(statusPath);

        when(criteriaBuilder.equal(
                statusPath,
                StatusMatricula.CONCLUIDA
        )).thenReturn(statusPredicate);

        Specification<Matricula> specification =
                MatriculaSpecification.comFiltros(
                        1L,
                        2L,
                        StatusMatricula.CONCLUIDA
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(criteriaBuilder).and(
                alunoPredicate,
                cursoPredicate,
                statusPredicate
        );
    }
}