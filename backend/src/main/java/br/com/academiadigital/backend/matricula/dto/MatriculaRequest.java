package br.com.academiadigital.backend.matricula.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MatriculaRequest {

    @NotNull(message = "O aluno é obrigatório.")
    @Positive(message = "O ID do aluno deve ser positivo.")
    private Long alunoId;

    @NotNull(message = "O curso é obrigatório.")
    @Positive(message = "O ID do curso deve ser positivo.")
    private Long cursoId;

    public MatriculaRequest() {
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }
}