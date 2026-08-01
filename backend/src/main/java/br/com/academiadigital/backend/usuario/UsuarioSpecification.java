package br.com.academiadigital.backend.usuario;

import org.springframework.data.jpa.domain.Specification;

public final class UsuarioSpecification {

    private UsuarioSpecification() {
    }

    public static Specification<Usuario> nomeContem(String nome) {
    return (root, query, criteriaBuilder) -> {

        if (nome == null || nome.isBlank()) {
            return criteriaBuilder.conjunction();
        }

        String nomeNormalizado = "%" + nome.trim().toLowerCase() + "%";

        return criteriaBuilder.like(
                criteriaBuilder.function(
                        "unaccent",
                        String.class,
                        criteriaBuilder.lower(root.get("nome"))
                ),
                criteriaBuilder.function(
                        "unaccent",
                        String.class,
                        criteriaBuilder.literal(nomeNormalizado)
                )
        );
    };
}

    public static Specification<Usuario> emailContem(String email) {
        return (root, query, criteriaBuilder) -> {

            if (email == null || email.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + email.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<Usuario> perfilIgual(String perfil) {
        return (root, query, criteriaBuilder) -> {

            if (perfil == null || perfil.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    criteriaBuilder.upper(root.get("perfil")),
                    perfil.trim().toUpperCase()
            );
        };
    }

    public static Specification<Usuario> ativoIgual(Boolean ativo) {
        return (root, query, criteriaBuilder) -> {

            if (ativo == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("ativo"),
                    ativo
            );
        };
    }
}