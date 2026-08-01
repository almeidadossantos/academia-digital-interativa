package br.com.academiadigital.backend.curso.dto;

import br.com.academiadigital.backend.curso.NivelCurso;
import br.com.academiadigital.backend.curso.StatusCurso;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CursoUpdateRequest {

    @NotBlank(message = "O título é obrigatório.")
    @Size(max = 180, message = "O título deve ter no máximo 180 caracteres.")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória.")
    @Size(max = 5000, message = "A descrição deve ter no máximo 5000 caracteres.")
    private String descricao;

    @NotNull(message = "A carga horária é obrigatória.")
    @Min(value = 1, message = "A carga horária deve ser maior que zero.")
    private Integer cargaHoraria;

    @NotNull(message = "O nível é obrigatório.")
    private NivelCurso nivel;

    private StatusCurso status;

    @Size(max = 500, message = "A URL da imagem deve ter no máximo 500 caracteres.")
    private String imagemUrl;

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

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public NivelCurso getNivel() {
        return nivel;
    }

    public void setNivel(NivelCurso nivel) {
        this.nivel = nivel;
    }

    public StatusCurso getStatus() {
        return status;
    }

    public void setStatus(StatusCurso status) {
        this.status = status;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }
}

