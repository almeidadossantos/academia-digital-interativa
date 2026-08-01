package br.com.academiadigital.backend.aula.dto;

import br.com.academiadigital.backend.aula.StatusAula;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AulaUpdateRequest {

    @NotNull(message = "O curso é obrigatório.")
    @Positive(message = "O ID do curso deve ser maior que zero.")
    private Long cursoId;

    @NotBlank(message = "O título é obrigatório.")
    @Size(
            max = 180,
            message = "O título deve possuir no máximo 180 caracteres."
    )
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória.")
    private String descricao;

    @NotNull(message = "A ordem da aula é obrigatória.")
    @Positive(message = "A ordem da aula deve ser maior que zero.")
    private Integer ordem;

    @NotNull(message = "A duração da aula é obrigatória.")
    @Positive(message = "A duração deve ser maior que zero.")
    private Integer duracaoMinutos;

    @Size(
            max = 500,
            message = "A URL do vídeo deve possuir no máximo 500 caracteres."
    )
    private String videoUrl;

    private StatusAula status;

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
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

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public StatusAula getStatus() {
        return status;
    }

    public void setStatus(StatusAula status) {
        this.status = status;
    }
}