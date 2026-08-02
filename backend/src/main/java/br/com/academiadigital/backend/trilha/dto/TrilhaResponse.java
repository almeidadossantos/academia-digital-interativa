package br.com.academiadigital.backend.trilha.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.academiadigital.backend.trilha.StatusTrilha;

public class TrilhaResponse {

    private Long id;

    private String titulo;

    private String descricao;

    private StatusTrilha status;

    private LocalDateTime dataCriacao;

    private LocalDateTime dataAtualizacao;

    private List<TrilhaCursoResponse> cursos =
            new ArrayList<>();

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

    public List<TrilhaCursoResponse> getCursos() {
        return cursos;
    }

    public void setCursos(
            List<TrilhaCursoResponse> cursos) {

        this.cursos = cursos;
    }
}