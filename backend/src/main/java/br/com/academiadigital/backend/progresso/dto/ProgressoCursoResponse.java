package br.com.academiadigital.backend.progresso.dto;

import java.util.ArrayList;
import java.util.List;

public class ProgressoCursoResponse {

    private Long matriculaId;

    private Long cursoId;

    private String cursoTitulo;

    private long totalAulas;

    private long aulasConcluidas;

    private double percentualConclusao;

    private List<ProgressoAulaResponse> aulas =
            new ArrayList<>();

    public ProgressoCursoResponse() {
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

    public long getTotalAulas() {
        return totalAulas;
    }

    public void setTotalAulas(long totalAulas) {
        this.totalAulas = totalAulas;
    }

    public long getAulasConcluidas() {
        return aulasConcluidas;
    }

    public void setAulasConcluidas(long aulasConcluidas) {
        this.aulasConcluidas = aulasConcluidas;
    }

    public double getPercentualConclusao() {
        return percentualConclusao;
    }

    public void setPercentualConclusao(
            double percentualConclusao) {

        this.percentualConclusao =
                percentualConclusao;
    }

    public List<ProgressoAulaResponse> getAulas() {
        return aulas;
    }

    public void setAulas(
            List<ProgressoAulaResponse> aulas) {

        this.aulas = aulas;
    }
}
