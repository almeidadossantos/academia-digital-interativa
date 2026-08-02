package br.com.academiadigital.backend.trilha;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "trilhas")
public class Trilha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            nullable = false,
            length = 150
    )
    private String titulo;

    @Column(
            nullable = false,
            length = 2000
    )
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private StatusTrilha status;

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

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();

        if (status == null) {
            status = StatusTrilha.RASCUNHO;
        }

        if (dataCriacao == null) {
            dataCriacao = agora;
        }

        dataAtualizacao = agora;
    }

    @PreUpdate
    public void preUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusTrilha getStatus() {
        return status;
    }

    public void setStatus(StatusTrilha status) {
        this.status = status;
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