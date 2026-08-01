package br.com.academiadigital.backend.aula;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public final class AulaSpecification {

    private AulaSpecification() {
    }

    public static Specification<Aula> comFiltros(
            Long cursoId,
            StatusAula status,
            String titulo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (cursoId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("curso").get("id"),
                                cursoId
                        )
                );
            }

            if (status != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            if (titulo != null && !titulo.isBlank()) {
                String tituloNormalizado = "%"
                        + titulo.trim().toLowerCase(Locale.ROOT)
                        + "%";

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("titulo")),
                                tituloNormalizado
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(Predicate[]::new)
            );
        };
    }
}