package br.com.academiadigital.backend.matricula.dto;

import java.time.LocalDateTime;

import br.com.academiadigital.backend.matricula.StatusMatricula;

public class MatriculaResponse {

    private Long id;

    private Long alunoId;

    private String alunoNome;

    private String alunoEmail;

    private Long cursoId;

    private String cursoTitulo;

    private StatusMatricula status;

    private LocalDateTime dataMatricula;

    private LocalDateTime dataConclusao;

    private LocalDateTime dataCancelamento;

    private LocalDateTime dataAtualizacao;

    public MatriculaResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public String getAlunoNome() {
        return alunoNome;
    }

    public void setAlunoNome(String alunoNome) {
        this.alunoNome = alunoNome;
    }

    public String getAlunoEmail() {
        return alunoEmail;
    }

    public void setAlunoEmail(String alunoEmail) {
        this.alunoEmail = alunoEmail;
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

    public StatusMatricula getStatus() {
        return status;
    }

    public void setStatus(StatusMatricula status) {
        this.status = status;
    }

    public LocalDateTime getDataMatricula() {
        return dataMatricula;
    }

    public void setDataMatricula(
            LocalDateTime dataMatricula) {

        this.dataMatricula = dataMatricula;
    }

    public LocalDateTime getDataConclusao() {
        return dataConclusao;
    }

    public void setDataConclusao(
            LocalDateTime dataConclusao) {

        this.dataConclusao = dataConclusao;
    }

    public LocalDateTime getDataCancelamento() {
        return dataCancelamento;
    }

    public void setDataCancelamento(
            LocalDateTime dataCancelamento) {

        this.dataCancelamento = dataCancelamento;
    }

    public LocalDateTime getDataAtualizacao() {
        return dataAtualizacao;
    }

    public void setDataAtualizacao(
            LocalDateTime dataAtualizacao) {

        this.dataAtualizacao = dataAtualizacao;
    }
}