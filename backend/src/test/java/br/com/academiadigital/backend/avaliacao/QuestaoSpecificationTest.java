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
class QuestaoSpecificationTest {

    @Mock
    private Root<Questao> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<Object> avaliacaoPath;

    @Mock
    private Path<Long> avaliacaoIdPath;

    @Mock
    private Path<TipoQuestao> tipoPath;

    @Mock
    private Path<String> enunciadoPath;

    @Mock
    private Expression<String> enunciadoEmMinusculas;

    @Mock
    private Predicate avaliacaoPredicate;

    @Mock
    private Predicate tipoPredicate;

    @Mock
    private Predicate enunciadoPredicate;

    @Mock
    private Predicate resultadoPredicate;

    @BeforeEach
    void configurarResultadoDaCombinacao() {
        when(criteriaBuilder.and(any(Predicate[].class)))
                .thenReturn(resultadoPredicate);
    }

    @Test
    void deveRetornarConjuncaoQuandoNaoHouverFiltros() {
        Specification<Questao> specification =
                QuestaoSpecification.comFiltros(
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
    void deveIgnorarEnunciadoEmBranco() {
        Specification<Questao> specification =
                QuestaoSpecification.comFiltros(
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
    void deveAplicarFiltroDeAvaliacao() {
        when(root.<Object>get("avaliacao"))
                .thenReturn(avaliacaoPath);

        when(avaliacaoPath.<Long>get("id"))
                .thenReturn(avaliacaoIdPath);

        when(criteriaBuilder.equal(
                avaliacaoIdPath,
                1L
        )).thenReturn(avaliacaoPredicate);

        Specification<Questao> specification =
                QuestaoSpecification.comFiltros(
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

        verify(root).get("avaliacao");
        verify(avaliacaoPath).get("id");

        verify(criteriaBuilder).equal(
                avaliacaoIdPath,
                1L
        );

        verify(criteriaBuilder).and(
                avaliacaoPredicate
        );
    }

    @Test
    void deveAplicarFiltroDeTipo() {
        when(root.<TipoQuestao>get("tipo"))
                .thenReturn(tipoPath);

        when(criteriaBuilder.equal(
                tipoPath,
                TipoQuestao.MULTIPLA_ESCOLHA
        )).thenReturn(tipoPredicate);

        Specification<Questao> specification =
                QuestaoSpecification.comFiltros(
                        null,
                        TipoQuestao.MULTIPLA_ESCOLHA,
                        null
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(root).get("tipo");

        verify(criteriaBuilder).equal(
                tipoPath,
                TipoQuestao.MULTIPLA_ESCOLHA
        );

        verify(criteriaBuilder).and(
                tipoPredicate
        );
    }

    @Test
    void deveAplicarFiltroDeEnunciadoIgnorandoMaiusculasEEspacos() {
        when(root.<String>get("enunciado"))
                .thenReturn(enunciadoPath);

        when(criteriaBuilder.lower(enunciadoPath))
                .thenReturn(enunciadoEmMinusculas);

        when(criteriaBuilder.like(
                enunciadoEmMinusculas,
                "%componente%"
        )).thenReturn(enunciadoPredicate);

        Specification<Questao> specification =
                QuestaoSpecification.comFiltros(
                        null,
                        null,
                        "  CoMpOnEnTe  "
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(root).get("enunciado");
        verify(criteriaBuilder).lower(
                enunciadoPath
        );

        verify(criteriaBuilder).like(
                enunciadoEmMinusculas,
                "%componente%"
        );

        verify(criteriaBuilder).and(
                enunciadoPredicate
        );
    }

    @Test
    void deveCombinarAvaliacaoTipoEEnunciado() {
        when(root.<Object>get("avaliacao"))
                .thenReturn(avaliacaoPath);

        when(avaliacaoPath.<Long>get("id"))
                .thenReturn(avaliacaoIdPath);

        when(criteriaBuilder.equal(
                avaliacaoIdPath,
                1L
        )).thenReturn(avaliacaoPredicate);

        when(root.<TipoQuestao>get("tipo"))
                .thenReturn(tipoPath);

        when(criteriaBuilder.equal(
                tipoPath,
                TipoQuestao.DISSERTATIVA
        )).thenReturn(tipoPredicate);

        when(root.<String>get("enunciado"))
                .thenReturn(enunciadoPath);

        when(criteriaBuilder.lower(enunciadoPath))
                .thenReturn(enunciadoEmMinusculas);

        when(criteriaBuilder.like(
                enunciadoEmMinusculas,
                "%explique%"
        )).thenReturn(enunciadoPredicate);

        Specification<Questao> specification =
                QuestaoSpecification.comFiltros(
                        1L,
                        TipoQuestao.DISSERTATIVA,
                        "Explique"
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(resultadoPredicate, resultado);

        verify(criteriaBuilder).and(
                avaliacaoPredicate,
                tipoPredicate,
                enunciadoPredicate
        );
    }
}
