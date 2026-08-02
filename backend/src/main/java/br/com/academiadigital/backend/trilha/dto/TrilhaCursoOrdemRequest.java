package br.com.academiadigital.backend.trilha.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class TrilhaCursoOrdemRequest {

    @NotNull(
            message = "A ordem do curso é obrigatória."
    )
    @Positive(
            message = "A ordem do curso deve ser positiva."
    )
    private Integer ordem;

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}