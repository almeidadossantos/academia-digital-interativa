package br.com.academiadigital.backend.trilha.dto;

public class TrilhaCursoResponse {

    private Long id;

    private Long cursoId;

    private String cursoTitulo;

    private Integer ordem;

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

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}