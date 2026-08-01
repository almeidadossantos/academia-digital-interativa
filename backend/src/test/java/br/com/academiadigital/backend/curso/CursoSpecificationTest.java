package br.com.academiadigital.backend.curso;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
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
class CursoSpecificationTest {

    @Mock
    private Root<Curso> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Predicate conjunctionPredicate;

    @Mock
    private Predicate tituloPredicate;

    @Mock
    private Predicate nivelPredicate;

    @Mock
    private Predicate statusPredicate;

    @Mock
    private Path<String> tituloPath;

    @Mock
    private Expression<String> tituloEmMinusculas;

    @Mock
    private Path<NivelCurso> nivelPath;

    @Mock
    private Path<StatusCurso> statusPath;

    @BeforeEach
    void configurarConjuncaoInicial() {
        when(criteriaBuilder.conjunction())
                .thenReturn(conjunctionPredicate);
    }

    @Test
    void deveRetornarConjuncaoQuandoTodosOsFiltrosForemNulos() {
        Specification<Curso> specification =
                CursoSpecification.comFiltros(null, null, null);

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(conjunctionPredicate, resultado);
        verify(criteriaBuilder).conjunction();
        verifyNoInteractions(root);
    }

    @Test
    void deveIgnorarTituloEmBranco() {
        Specification<Curso> specification =
                CursoSpecification.comFiltros("   ", null, null);

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(conjunctionPredicate, resultado);
        verifyNoInteractions(root);
    }

    @Test
    void deveAplicarFiltroDeTituloIgnorandoMaiusculasEEspacos() {
        when(root.<String>get("titulo"))
                .thenReturn(tituloPath);
        when(criteriaBuilder.lower(tituloPath))
                .thenReturn(tituloEmMinusculas);
        when(criteriaBuilder.like(
                tituloEmMinusculas,
                "%java%"
        )).thenReturn(tituloPredicate);

        Specification<Curso> specification =
                CursoSpecification.comFiltros(
                        "  JaVa  ",
                        null,
                        null
                );

        assertDoesNotThrow(() ->
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                )
        );

        verify(root).get("titulo");
        verify(criteriaBuilder).lower(tituloPath);
        verify(criteriaBuilder).like(
                tituloEmMinusculas,
                "%java%"
        );
    }

    @Test
    void deveAplicarFiltroDeNivel() {
        when(root.<NivelCurso>get("nivel"))
                .thenReturn(nivelPath);
        when(criteriaBuilder.equal(
                nivelPath,
                NivelCurso.INTERMEDIARIO
        )).thenReturn(nivelPredicate);

        Specification<Curso> specification =
                CursoSpecification.comFiltros(
                        null,
                        NivelCurso.INTERMEDIARIO,
                        null
                );

        assertDoesNotThrow(() ->
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                )
        );

        verify(root).get("nivel");
        verify(criteriaBuilder).equal(
                nivelPath,
                NivelCurso.INTERMEDIARIO
        );
    }

    @Test
    void deveAplicarFiltroDeStatus() {
        when(root.<StatusCurso>get("status"))
                .thenReturn(statusPath);
        when(criteriaBuilder.equal(
                statusPath,
                StatusCurso.PUBLICADO
        )).thenReturn(statusPredicate);

        Specification<Curso> specification =
                CursoSpecification.comFiltros(
                        null,
                        null,
                        StatusCurso.PUBLICADO
                );

        assertDoesNotThrow(() ->
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                )
        );

        verify(root).get("status");
        verify(criteriaBuilder).equal(
                statusPath,
                StatusCurso.PUBLICADO
        );
    }

    @Test
    void deveCombinarTituloNivelEStatus() {
        when(root.<String>get("titulo"))
                .thenReturn(tituloPath);
        when(criteriaBuilder.lower(tituloPath))
                .thenReturn(tituloEmMinusculas);
        when(criteriaBuilder.like(
                tituloEmMinusculas,
                "%spring%"
        )).thenReturn(tituloPredicate);

        when(root.<NivelCurso>get("nivel"))
                .thenReturn(nivelPath);
        when(criteriaBuilder.equal(
                nivelPath,
                NivelCurso.AVANCADO
        )).thenReturn(nivelPredicate);

        when(root.<StatusCurso>get("status"))
                .thenReturn(statusPath);
        when(criteriaBuilder.equal(
                statusPath,
                StatusCurso.PUBLICADO
        )).thenReturn(statusPredicate);

        Specification<Curso> specification =
                CursoSpecification.comFiltros(
                        "Spring",
                        NivelCurso.AVANCADO,
                        StatusCurso.PUBLICADO
                );

        assertDoesNotThrow(() ->
                specification.toPredicate(
                        root,
                        query,
                        criteriaBuilder
                )
        );

        verify(criteriaBuilder).like(
                tituloEmMinusculas,
                "%spring%"
        );
        verify(criteriaBuilder).equal(
                nivelPath,
                NivelCurso.AVANCADO
        );
        verify(criteriaBuilder).equal(
                statusPath,
                StatusCurso.PUBLICADO
        );
    }
}