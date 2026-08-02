package br.com.academiadigital.backend.trilha;

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
class TrilhaSpecificationTest {

    @Mock
    private Root<Trilha> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<String> tituloPath;

    @Mock
    private Expression<String> tituloEmMinusculas;

    @Mock
    private Path<StatusTrilha> statusPath;

    @Mock
    private Predicate tituloPredicate;

    @Mock
    private Predicate statusPredicate;

    @Mock
    private Predicate resultadoPredicate;

    @BeforeEach
    void configurarResultadoDaCombinacao() {
        when(criteriaBuilder.and(
                any(Predicate[].class)
        )).thenReturn(resultadoPredicate);
    }

    @Test
    void deveRetornarConjuncaoQuandoNaoHouverFiltros() {
        Specification<Trilha> specification =
                TrilhaSpecification.comFiltros(
                        null,
                        null
                );

        Predicate resultado =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(
                resultadoPredicate,
                resultado
        );

        verify(criteriaBuilder).and();
        verifyNoInteractions(root);
    }

    @Test
    void deveFiltrarPorTituloNormalizado() {
        when(root.<String>get("titulo"))
                .thenReturn(tituloPath);

        when(criteriaBuilder.lower(tituloPath))
                .thenReturn(tituloEmMinusculas);

        when(criteriaBuilder.like(
                tituloEmMinusculas,
                "%java%"
        )).thenReturn(tituloPredicate);

        Specification<Trilha> specification =
                TrilhaSpecification.comFiltros(
                        "  JAVA  ",
                        null
                );

        Predicate resultado =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(
                resultadoPredicate,
                resultado
        );

        verify(criteriaBuilder).and(
                tituloPredicate
        );
    }

    @Test
    void deveFiltrarPorStatus() {
        when(root.<StatusTrilha>get("status"))
                .thenReturn(statusPath);

        when(criteriaBuilder.equal(
                statusPath,
                StatusTrilha.PUBLICADA
        )).thenReturn(statusPredicate);

        Specification<Trilha> specification =
                TrilhaSpecification.comFiltros(
                        null,
                        StatusTrilha.PUBLICADA
                );

        Predicate resultado =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(
                resultadoPredicate,
                resultado
        );

        verify(criteriaBuilder).and(
                statusPredicate
        );
    }

    @Test
    void deveCombinarTituloEStatus() {
        when(root.<String>get("titulo"))
                .thenReturn(tituloPath);

        when(criteriaBuilder.lower(tituloPath))
                .thenReturn(tituloEmMinusculas);

        when(criteriaBuilder.like(
                tituloEmMinusculas,
                "%java%"
        )).thenReturn(tituloPredicate);

        when(root.<StatusTrilha>get("status"))
                .thenReturn(statusPath);

        when(criteriaBuilder.equal(
                statusPath,
                StatusTrilha.PUBLICADA
        )).thenReturn(statusPredicate);

        Specification<Trilha> specification =
                TrilhaSpecification.comFiltros(
                        "Java",
                        StatusTrilha.PUBLICADA
                );

        Predicate resultado =
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                );

        assertSame(
                resultadoPredicate,
                resultado
        );

        verify(criteriaBuilder).like(
                tituloEmMinusculas,
                "%java%"
        );

        verify(criteriaBuilder).equal(
                statusPath,
                StatusTrilha.PUBLICADA
        );

        verify(criteriaBuilder).and(
                any(Predicate[].class)
        );
    }
}