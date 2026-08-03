package br.com.academiadigital.backend.avaliacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.academiadigital.backend.avaliacao.StatusAvaliacao;

public class AvaliacaoResponse {

    private Long id;
    private Long cursoId;
    private String cursoTitulo;
    private String titulo;
    private String descricao;
    private Integer ordem;
    private BigDecimal notaMinima;
    private Integer maximoTentativas;
    private Integer tempoLimiteMinutos;
    private StatusAvaliacao status;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public String getCursoTitulo() {
        return cursoTitulo;
    }

    public void setCursoTitulo(String cursoTitulo) {
        this.cursoTitulo = cursoTitulo;
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

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public BigDecimal getNotaMinima() {
        return notaMinima;
    }

    public void setNotaMinima(BigDecimal notaMinima) {
        this.notaMinima = notaMinima;
    }

    public Integer getMaximoTentativas() {
        return maximoTentativas;
    }

    public void setMaximoTentativas(
            Integer maximoTentativas) {
        this.maximoTentativas = maximoTentativas;
    }

    public Integer getTempoLimiteMinutos() {
        return tempoLimiteMinutos;
    }

    public void setTempoLimiteMinutos(
            Integer tempoLimiteMinutos) {
        this.tempoLimiteMinutos = tempoLimiteMinutos;
    }

    public StatusAvaliacao getStatus() {
        return status;
    }

    public void setStatus(StatusAvaliacao status) {
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
