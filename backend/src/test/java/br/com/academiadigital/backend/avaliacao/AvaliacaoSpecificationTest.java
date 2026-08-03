package br.com.academiadigital.backend.avaliacao;

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
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
class AvaliacaoSpecificationTest {

    @Mock
    private Root<Avaliacao> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> cursoPath;

    @Mock
    private Path<Long> cursoIdPath;

    @Mock
    private Path<StatusAvaliacao> statusPath;

    @Mock
    private Path<String> tituloPath;

    @Mock
    private Expression<String> tituloEmMinusculas;

    @Mock
    private Predicate cursoPredicate;

    @Mock
    private Predicate statusPredicate;

    @Mock
    private Predicate tituloPredicate;

    @Mock
    private Predicate resultadoPredicate;

    @BeforeEach
    void configurarResultadoDaCombinacao() {
        when(criteriaBuilder.and(any(Predicate[].class)))
                .thenReturn(resultadoPredicate);
    }

    @Test
    void deveRetornarConjuncaoQuandoNaoHouverFiltros() {
        Specification<Avaliacao> specification =
                AvaliacaoSpecification.comFiltros(
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
    void deveIgnorarTituloEmBranco() {
        Specification<Avaliacao> specification =
                AvaliacaoSpecification.comFiltros(
                        null,
                        null,
                        "   "
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
    void deveAplicarFiltroDeCurso() {
        when(root.<Object>get("curso"))
                .thenReturn(cursoPath);

        when(cursoPath.<Long>get("id"))
                .thenReturn(cursoIdPath);

        when(criteriaBuilder.equal(
                cursoIdPath,
                1L
        )).thenReturn(cursoPredicate);

        Specification<Avaliacao> specification =
                AvaliacaoSpecification.comFiltros(
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

        verify(root).get("curso");
        verify(cursoPath).get("id");

        verify(criteriaBuilder).equal(
                cursoIdPath,
                1L
        );

        verify(criteriaBuilder).and(
                cursoPredicate
        );
    }

    @Test
    void deveAplicarFiltroDeStatus() {
        when(root.<StatusAvaliacao>get("status"))
                .thenReturn(statusPath);

        when(criteriaBuilder.equal(
                statusPath,
                StatusAvaliacao.PUBLICADA
        )).thenReturn(statusPredicate);

        Specification<Avaliacao> specification =
                AvaliacaoSpecification.comFiltros(
                        null,
                        StatusAvaliacao.PUBLICADA,
                        null
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(root).get("status");

        verify(criteriaBuilder).equal(
                statusPath,
                StatusAvaliacao.PUBLICADA
        );

        verify(criteriaBuilder).and(
                statusPredicate
        );
    }

    @Test
    void deveAplicarFiltroDeTituloIgnorandoMaiusculasEEspacos() {
        when(root.<String>get("titulo"))
                .thenReturn(tituloPath);

        when(criteriaBuilder.lower(tituloPath))
                .thenReturn(tituloEmMinusculas);

        when(criteriaBuilder.like(
                tituloEmMinusculas,
                "%conhecimentos%"
        )).thenReturn(tituloPredicate);

        Specification<Avaliacao> specification =
                AvaliacaoSpecification.comFiltros(
                        null,
                        null,
                        "  CoNhEcImEnToS  "
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(root).get("titulo");
        verify(criteriaBuilder).lower(tituloPath);

        verify(criteriaBuilder).like(
                tituloEmMinusculas,
                "%conhecimentos%"
        );

        verify(criteriaBuilder).and(
                tituloPredicate
        );
    }

    @Test
    void deveCombinarCursoStatusETitulo() {
        when(root.<Object>get("curso"))
                .thenReturn(cursoPath);

        when(cursoPath.<Long>get("id"))
                .thenReturn(cursoIdPath);

        when(criteriaBuilder.equal(
                cursoIdPath,
                1L
        )).thenReturn(cursoPredicate);

        when(root.<StatusAvaliacao>get("status"))
                .thenReturn(statusPath);

        when(criteriaBuilder.equal(
                statusPath,
                StatusAvaliacao.PUBLICADA
        )).thenReturn(statusPredicate);

        when(root.<String>get("titulo"))
                .thenReturn(tituloPath);

        when(criteriaBuilder.lower(tituloPath))
                .thenReturn(tituloEmMinusculas);

        when(criteriaBuilder.like(
                tituloEmMinusculas,
                "%final%"
        )).thenReturn(tituloPredicate);

        Specification<Avaliacao> specification =
                AvaliacaoSpecification.comFiltros(
                        1L,
                        StatusAvaliacao.PUBLICADA,
                        "Final"
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(criteriaBuilder).and(
                cursoPredicate,
                statusPredicate,
                tituloPredicate
        );
    }
}
