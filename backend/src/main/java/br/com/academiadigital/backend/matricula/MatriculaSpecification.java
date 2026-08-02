package br.com.academiadigital.backend.matricula;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

public final class MatriculaSpecification {

    private MatriculaSpecification() {
    }

    public static Specification<Matricula> comFiltros(
            Long alunoId,
            Long cursoId,
            StatusMatricula status) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (alunoId != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("aluno").get("id"),
                                alunoId
                        )
                );
            }

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

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}