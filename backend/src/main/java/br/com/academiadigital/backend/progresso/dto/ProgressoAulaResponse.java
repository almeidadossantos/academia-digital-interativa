package br.com.academiadigital.backend.progresso.dto;

import java.time.LocalDateTime;

public class ProgressoAulaResponse {

    private Long id;

    private Long matriculaId;

    private Long cursoId;

    private String cursoTitulo;

    private Long aulaId;

    private String aulaTitulo;

    private Integer aulaOrdem;

    private Boolean concluida;

    private LocalDateTime dataConclusao;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    public ProgressoAulaResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMatriculaId() {
        return matriculaId;
    }

    public void setMatriculaId(Long matriculaId) {
        this.matriculaId = matriculaId;
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

    public Long getAulaId() {
        return aulaId;
    }

    public void setAulaId(Long aulaId) {
        this.aulaId = aulaId;
    }

    public String getAulaTitulo() {
        return aulaTitulo;
    }

    public void setAulaTitulo(String aulaTitulo) {
        this.aulaTitulo = aulaTitulo;
    }

    public Integer getAulaOrdem() {
        return aulaOrdem;
    }

    public void setAulaOrdem(Integer aulaOrdem) {
        this.aulaOrdem = aulaOrdem;
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