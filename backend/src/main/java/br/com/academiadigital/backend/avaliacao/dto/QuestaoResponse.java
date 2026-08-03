package br.com.academiadigital.backend.avaliacao.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import br.com.academiadigital.backend.avaliacao.TipoQuestao;

public class QuestaoResponse {

    private Long id;
    private Long avaliacaoId;
    private String avaliacaoTitulo;
    private String enunciado;
    private TipoQuestao tipo;
    private Integer ordem;
    private BigDecimal pontuacao;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAvaliacaoId() {
        return avaliacaoId;
    }

    public void setAvaliacaoId(Long avaliacaoId) {
        this.avaliacaoId = avaliacaoId;
    }

    public String getAvaliacaoTitulo() {
        return avaliacaoTitulo;
    }

    public void setAvaliacaoTitulo(
            String avaliacaoTitulo) {

        this.avaliacaoTitulo = avaliacaoTitulo;
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
