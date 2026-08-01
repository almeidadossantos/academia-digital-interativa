package br.com.academiadigital.backend.curso;

import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

public final class CursoSpecification {

    private CursoSpecification() {
    }

    public static Specification<Curso> comFiltros(
            String titulo,
            NivelCurso nivel,
            StatusCurso status) {

        Specification<Curso> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.conjunction();

        if (titulo != null && !titulo.isBlank()) {
            String tituloNormalizado =
                    "%" + titulo.trim().toLowerCase(Locale.ROOT) + "%";

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.like(
                                    criteriaBuilder.lower(
                                            root.get("titulo")
                                    ),
                                    tituloNormalizado
                            )
            );
        }

        if (nivel != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("nivel"),
                                    nivel
                            )
            );
        }

        if (status != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("status"),
                                    status
                            )
            );
        }

        return specification;
    }
}