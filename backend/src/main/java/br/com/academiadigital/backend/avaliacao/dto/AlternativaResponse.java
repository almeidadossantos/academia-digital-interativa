package br.com.academiadigital.backend.avaliacao.dto;

import java.time.LocalDateTime;

public class AlternativaResponse {

    private Long id;
    private Long questaoId;
    private String questaoEnunciado;
    private String texto;
    private Boolean correta;
    private Integer ordem;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAtualizacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestaoId() {
        return questaoId;
    }

    public void setQuestaoId(Long questaoId) {
        this.questaoId = questaoId;
    }

    public String getQuestaoEnunciado() {
        return questaoEnunciado;
    }

    public void setQuestaoEnunciado(
            String questaoEnunciado) {

        this.questaoEnunciado = questaoEnunciado;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Boolean getCorreta() {
        return correta;
    }

    public void setCorreta(Boolean correta) {
        this.correta = correta;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
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
