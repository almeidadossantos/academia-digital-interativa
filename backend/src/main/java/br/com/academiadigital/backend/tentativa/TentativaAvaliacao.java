package br.com.academiadigital.backend.tentativa;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.academiadigital.backend.avaliacao.Avaliacao;
import br.com.academiadigital.backend.matricula.Matricula;
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
        name = "tentativas_avaliacao",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tentativas_matricula_avaliacao_numero",
                        columnNames = {
                                "matricula_id",
                                "avaliacao_id",
                                "numero_tentativa"
                        }
                )
        }
)
public class TentativaAvaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "matricula_id", nullable = false)
    private Matricula matricula;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao;

    @Column(name = "numero_tentativa", nullable = false)
    private Integer numeroTentativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusTentativa status;

    @Column(name = "data_inicio", nullable = false, updatable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_limite")
    private LocalDateTime dataLimite;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    @Column(name = "pontuacao_total", precision = 7, scale = 2)
    private BigDecimal pontuacaoTotal;

    @Column(name = "pontuacao_obtida", precision = 7, scale = 2)
    private BigDecimal pontuacaoObtida;

    @Column(precision = 5, scale = 2)
    private BigDecimal nota;

    private Boolean aprovado;

    @Column(name = "data_atualizacao", nullable = false)
    private LocalDateTime dataAtualizacao;

    public TentativaAvaliacao() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime agora = LocalDateTime.now();

        if (status == null) {
            status = StatusTentativa.EM_ANDAMENTO;
        }

        if (dataInicio == null) {
            dataInicio = agora;
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

    public Matricula getMatricula() {
        return matricula;
    }

    public void setMatricula(Matricula matricula) {
        this.matricula = matricula;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Integer getNumeroTentativa() {
        return numeroTentativa;
    }

    public void setNumeroTentativa(Integer numeroTentativa) {
        this.numeroTentativa = numeroTentativa;
    }

    public StatusTentativa getStatus() {
        return status;
    }

    public void setStatus(StatusTentativa status) {
        this.status = status;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(LocalDateTime dataLimite) {
        this.dataLimite = dataLimite;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public LocalDateTime getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public BigDecimal getPontuacaoTotal() {
        return pontuacaoTotal;
    }

    public void setPontuacaoTotal(BigDecimal pontuacaoTotal) {
        this.pontuacaoTotal = pontuacaoTotal;
    }

    public BigDecimal getPontuacaoObtida() {
        return pontuacaoObtida;
    }

    public void setPontuacaoObtida(BigDecimal pontuacaoObtida) {
        this.pontuacaoObtida = pontuacaoObtida;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public Boolean getAprovado() {
        return aprovado;
    }

    public void setAprovado(Boolean aprovado) {
        this.aprovado = aprovado;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(LocalDateTime dataAtualizacao) {
        this.dataAtualizacao = dataAtualizacao;
    }
}
