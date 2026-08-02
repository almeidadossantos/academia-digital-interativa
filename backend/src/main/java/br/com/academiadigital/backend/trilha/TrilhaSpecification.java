package br.com.academiadigital.backend.trilha;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public final class TrilhaSpecification {

    private TrilhaSpecification() {
    }

    public static Specification<Trilha> comFiltros(
            String titulo,
            StatusTrilha status) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates =
                    new ArrayList<>();

            if (titulo != null
                    && !titulo.isBlank()) {

                String tituloNormalizado =
                        titulo.trim()
                                .toLowerCase(Locale.ROOT);

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("titulo")
                                ),
                                "%"
                                        + tituloNormalizado
                                        + "%"
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

            return criteriaBuilder.and(
                    predicates.toArray(
                            Predicate[]::new
                    )
            );
        };
    }
}