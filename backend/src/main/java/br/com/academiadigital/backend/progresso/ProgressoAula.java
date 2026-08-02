package br.com.academiadigital.backend.progresso;

import java.time.LocalDateTime;

import br.com.academiadigital.backend.aula.Aula;
import br.com.academiadigital.backend.matricula.Matricula;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "progressos_aulas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_progressos_matricula_aula",
                        columnNames = {
                                "matricula_id",
                                "aula_id"
                        }
                )
        }
)
public class ProgressoAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "matricula_id",
            nullable = false
    )
    private Matricula matricula;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "aula_id",
            nullable = false
    )
    private Aula aula;

    @Column(nullable = false)
    private Boolean concluida;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;

    @Column(
            name = "data_criacao",
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataCriacao;

    @Column(
            name = "data_atualizacao",
            nullable = false
    )
    private LocalDateTime dataAtualizacao;

    public ProgressoAula() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();

        if (concluida == null) {
            concluida = false;
        }

        atualizarDataConclusao(agora);

        if (dataCriacao == null) {
            dataCriacao = agora;
        }

        dataAtualizacao = agora;
    }

    @PreUpdate
    public void preUpdate() {
        LocalDateTime agora = LocalDateTime.now();

        atualizarDataConclusao(agora);
        dataAtualizacao = agora;
    }

    private void atualizarDataConclusao(
            LocalDateTime agora) {

        if (Boolean.TRUE.equals(concluida)) {
            if (dataConclusao == null) {
                dataConclusao = agora;
            }

            return;
        }

        dataConclusao = null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Matricula getMatricula() {
        return matricula;
    }

    public void setMatricula(Matricula matricula) {
        this.matricula = matricula;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public Boolean getConcluida() {
        return concluida;
    }

    public void setConcluida(Boolean concluida) {
        this.concluida = concluida;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(
            LocalDateTime dataConclusao) {

        this.dataConclusao = dataConclusao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(
            LocalDateTime dataCriacao) {

        this.dataCriacao = dataCriacao;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(
            LocalDateTime dataAtualizacao) {

        this.dataAtualizacao = dataAtualizacao;
    }
}