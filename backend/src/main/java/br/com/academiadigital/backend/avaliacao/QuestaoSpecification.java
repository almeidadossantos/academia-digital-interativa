package br.com.academiadigital.backend.avaliacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public final class QuestaoSpecification {

    private QuestaoSpecification() {
    }

    public static Specification<Questao> comFiltros(
            Long avaliacaoId,
            TipoQuestao tipo,
            String enunciado
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates =
                    new ArrayList<>();

            if (avaliacaoId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("avaliacao").get("id"),
                                avaliacaoId
                        )
                );
            }

            if (tipo != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("tipo"),
                                tipo
                        )
                );
            }

            if (enunciado != null
                    && !enunciado.isBlank()) {

                String enunciadoNormalizado = "%"
                        + enunciado.trim()
                                .toLowerCase(Locale.ROOT)
                        + "%";

                predicates.add(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(
                                        root.get("enunciado")
                                ),
                                enunciadoNormalizado
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
