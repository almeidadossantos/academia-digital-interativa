package br.com.academiadigital.backend.usuario;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class UsuarioSpecificationTest {

    @Mock
    private Root<Usuario> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder criteriaBuilder;

    @Mock
    private Path<String> caminhoString;

    @Mock
    private Path<Boolean> caminhoBoolean;

    @Mock
    private Expression<String> expressaoLower;

    @Mock
    private Expression<String> expressaoUpper;

    @Mock
    private Expression<String> expressaoLiteral;

    @Mock
    private Expression<String> expressaoNomeSemAcento;

    @Mock
    private Expression<String> expressaoLiteralSemAcento;

    @Mock
    private Predicate predicate;

    @Test
    void deveRetornarConjunctionQuandoNomeForNulo() {
        when(criteriaBuilder.conjunction())
                .thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.nomeContem(null);

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void deveRetornarConjunctionQuandoNomeForVazio() {
        when(criteriaBuilder.conjunction())
                .thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.nomeContem("   ");

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void deveCriarFiltroPorNomeNormalizado() {
        when(root.<String>get("nome"))
                .thenReturn(caminhoString);

        when(criteriaBuilder.lower(caminhoString))
                .thenReturn(expressaoLower);

       when(criteriaBuilder.literal("%joão%"))
        .thenReturn(expressaoLiteral);

when(criteriaBuilder.function(
        eq("unaccent"),
        eq(String.class),
        org.mockito.ArgumentMatchers.<Expression<?>>any()
)).thenReturn(
        expressaoNomeSemAcento,
        expressaoLiteralSemAcento
);

when(criteriaBuilder.like(
        expressaoNomeSemAcento,
        expressaoLiteralSemAcento
)).thenReturn(predicate);
        Specification<Usuario> specification =
                UsuarioSpecification.nomeContem(" João ");

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);

        verify(root).get("nome");
        verify(criteriaBuilder).lower(caminhoString);
        verify(criteriaBuilder).literal("%joão%");

        verify(criteriaBuilder).function(
                eq("unaccent"),
                eq(String.class),
                eq(expressaoLower)
        );

        verify(criteriaBuilder).function(
                eq("unaccent"),
                eq(String.class),
                eq(expressaoLiteral)
        );

        verify(criteriaBuilder).like(
                expressaoNomeSemAcento,
                expressaoLiteralSemAcento
        );
    }

    @Test
    void deveRetornarConjunctionQuandoEmailForNulo() {
        when(criteriaBuilder.conjunction())
                .thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.emailContem(null);

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void deveRetornarConjunctionQuandoEmailForVazio() {
        when(criteriaBuilder.conjunction())
                .thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.emailContem(" ");

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void deveCriarFiltroPorEmailNormalizado() {
        when(root.<String>get("email"))
                .thenReturn(caminhoString);

        when(criteriaBuilder.lower(caminhoString))
                .thenReturn(expressaoLower);

        when(criteriaBuilder.like(
                expressaoLower,
                "%joao@email.com%"
        )).thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.emailContem(
                        " JOAO@EMAIL.COM "
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);

        verify(root).get("email");
        verify(criteriaBuilder).lower(caminhoString);

        verify(criteriaBuilder).like(
                expressaoLower,
                "%joao@email.com%"
        );
    }

    @Test
    void deveRetornarConjunctionQuandoPerfilForNulo() {
        when(criteriaBuilder.conjunction())
                .thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.perfilIgual(null);

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void deveRetornarConjunctionQuandoPerfilForVazio() {
        when(criteriaBuilder.conjunction())
                .thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.perfilIgual(" ");

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void deveCriarFiltroPorPerfilNormalizado() {
        when(root.<String>get("perfil"))
                .thenReturn(caminhoString);

        when(criteriaBuilder.upper(caminhoString))
                .thenReturn(expressaoUpper);

        when(criteriaBuilder.equal(
                expressaoUpper,
                "PROFESSOR"
        )).thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.perfilIgual(
                        " professor "
                );

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);

        verify(root).get("perfil");
        verify(criteriaBuilder).upper(caminhoString);

        verify(criteriaBuilder).equal(
                expressaoUpper,
                "PROFESSOR"
        );
    }

    @Test
    void deveRetornarConjunctionQuandoAtivoForNulo() {
        when(criteriaBuilder.conjunction())
                .thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.ativoIgual(null);

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);
        verify(criteriaBuilder).conjunction();
    }

    @Test
    void deveCriarFiltroQuandoAtivoForVerdadeiro() {
        when(root.<Boolean>get("ativo"))
                .thenReturn(caminhoBoolean);

        when(criteriaBuilder.equal(
                caminhoBoolean,
                true
        )).thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.ativoIgual(true);

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);

        verify(root).get("ativo");

        verify(criteriaBuilder).equal(
                caminhoBoolean,
                true
        );
    }

    @Test
    void deveCriarFiltroQuandoAtivoForFalso() {
        when(root.<Boolean>get("ativo"))
                .thenReturn(caminhoBoolean);

        when(criteriaBuilder.equal(
                caminhoBoolean,
                false
        )).thenReturn(predicate);

        Specification<Usuario> specification =
                UsuarioSpecification.ativoIgual(false);

        Predicate resultado = specification.toPredicate(
                root,
                query,
                criteriaBuilder
        );

        assertSame(predicate, resultado);

        verify(root).get("ativo");

        verify(criteriaBuilder).equal(
                caminhoBoolean,
                false
        );
    }
}