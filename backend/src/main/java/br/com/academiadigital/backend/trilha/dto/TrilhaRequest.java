package br.com.academiadigital.backend.trilha.dto;

import br.com.academiadigital.backend.trilha.StatusTrilha;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class TrilhaRequest {

    @NotBlank(
            message = "O título da trilha é obrigatório."
    )
    @Size(
            max = 150,
            message = "O título da trilha deve ter no máximo 150 caracteres."
    )
    private String titulo;

    @NotBlank(
            message = "A descrição da trilha é obrigatória."
    )
    @Size(
            max = 2000,
            message = "A descrição da trilha deve ter no máximo 2000 caracteres."
    )
    private String descricao;

    private StatusTrilha status;

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
}