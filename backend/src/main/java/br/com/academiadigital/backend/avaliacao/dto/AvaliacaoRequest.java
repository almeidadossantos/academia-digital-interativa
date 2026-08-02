package br.com.academiadigital.backend.avaliacao.dto;

import java.math.BigDecimal;

import br.com.academiadigital.backend.avaliacao.StatusAvaliacao;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AvaliacaoRequest {

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

    @NotNull(message = "A ordem da avaliação é obrigatória.")
    @Positive(message = "A ordem da avaliação deve ser maior que zero.")
    private Integer ordem;

    @NotNull(message = "A nota mínima é obrigatória.")
    @DecimalMin(
            value = "0.00",
            message = "A nota mínima não pode ser menor que zero."
    )
    @DecimalMax(
            value = "10.00",
            message = "A nota mínima não pode ser maior que dez."
    )
    @Digits(
            integer = 2,
            fraction = 2,
            message = "A nota mínima deve possuir no máximo duas casas decimais."
    )
    private BigDecimal notaMinima;

    @NotNull(message = "O máximo de tentativas é obrigatório.")
    @Positive(message = "O máximo de tentativas deve ser maior que zero.")
    private Integer maximoTentativas;

    @Positive(message = "O tempo limite deve ser maior que zero.")
    private Integer tempoLimiteMinutos;

    private StatusAvaliacao status;

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

    public BigDecimal getNotaMinima() {
        return notaMinima;
    }

    public void setNotaMinima(BigDecimal notaMinima) {
        this.notaMinima = notaMinima;
    }

    public Integer getMaximoTentativas() {
        return maximoTentativas;
    }

    public void setMaximoTentativas(
            Integer maximoTentativas) {
        this.maximoTentativas = maximoTentativas;
    }

    public Integer getTempoLimiteMinutos() {
        return tempoLimiteMinutos;
    }

    public void setTempoLimiteMinutos(
            Integer tempoLimiteMinutos) {
        this.tempoLimiteMinutos = tempoLimiteMinutos;
    }

    public StatusAvaliacao getStatus() {
        return status;
    }

    public void setStatus(StatusAvaliacao status) {
        this.status = status;
    }
}
