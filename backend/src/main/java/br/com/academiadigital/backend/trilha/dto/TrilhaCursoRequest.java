package br.com.academiadigital.backend.trilha.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TrilhaCursoRequest {

    @NotNull(
            message = "O curso é obrigatório."
    )
    @Positive(
            message = "O identificador do curso deve ser positivo."
    )
    private Long cursoId;

    @NotNull(
            message = "A ordem do curso é obrigatória."
    )
    @Positive(
            message = "A ordem do curso deve ser positiva."
    )
    private Integer ordem;

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}