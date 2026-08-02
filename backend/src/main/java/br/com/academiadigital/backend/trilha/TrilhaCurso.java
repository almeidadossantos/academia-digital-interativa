package br.com.academiadigital.backend.trilha;

import br.com.academiadigital.backend.curso.Curso;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "trilha_cursos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_trilha_curso",
                        columnNames = {
                                "trilha_id",
                                "curso_id"
                        }
                ),
                @UniqueConstraint(
                        name = "uk_trilha_ordem",
                        columnNames = {
                                "trilha_id",
                                "ordem"
                        }
                )
        }
)
public class TrilhaCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "trilha_id",
            nullable = false
    )
    private Trilha trilha;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "curso_id",
            nullable = false
    )
    private Curso curso;

    @Column(nullable = false)
    private Integer ordem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trilha getTrilha() {
        return trilha;
    }

    public void setTrilha(Trilha trilha) {
        this.trilha = trilha;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}