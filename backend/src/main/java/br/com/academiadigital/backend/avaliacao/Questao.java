package br.com.academiadigital.backend.avaliacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "questoes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_questoes_avaliacao_ordem",
                        columnNames = {
                                "avaliacao_id",
                                "ordem"
                        }
                )
        }
)
public class Questao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "avaliacao_id",
            nullable = false
    )
    private Avaliacao avaliacao;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String enunciado;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private TipoQuestao tipo;

    @Column(nullable = false)
    private Integer ordem;

    @Column(
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal pontuacao;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private LocalDateTime dataAtualizacao;

    public Questao() {
    }

    @PrePersist
    void prePersist() {
        LocalDateTime agora = LocalDateTime.now();

        if (dataCriacao == null) {
            dataCriacao = agora;
        }

        dataAtualizacao = agora;
    }

    @PreUpdate
    void preUpdate() {
        dataAtualizacao = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public TipoQuestao getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuestao tipo) {
        this.tipo = tipo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public BigDecimal getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(BigDecimal pontuacao) {
        this.pontuacao = pontuacao;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
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
