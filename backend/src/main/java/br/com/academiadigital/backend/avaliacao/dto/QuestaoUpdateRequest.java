package br.com.academiadigital.backend.avaliacao.dto;

import java.math.BigDecimal;

import br.com.academiadigital.backend.avaliacao.TipoQuestao;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class QuestaoUpdateRequest {

    @NotNull(message = "A avaliação é obrigatória.")
    @Positive(
            message = "O ID da avaliação deve ser maior que zero."
    )
    private Long avaliacaoId;

    @NotBlank(message = "O enunciado é obrigatório.")
    private String enunciado;

    @NotNull(message = "O tipo da questão é obrigatório.")
    private TipoQuestao tipo;

    @NotNull(message = "A ordem da questão é obrigatória.")
    @Positive(
            message = "A ordem da questão deve ser maior que zero."
    )
    private Integer ordem;

    @NotNull(message = "A pontuação é obrigatória.")
    @DecimalMin(
            value = "0.01",
            message = "A pontuação deve ser maior que zero."
    )
    @DecimalMax(
            value = "10.00",
            message = "A pontuação não pode ser maior que dez."
    )
    @Digits(
            integer = 2,
            fraction = 2,
            message = "A pontuação deve possuir no máximo duas casas decimais."
    )
    private BigDecimal pontuacao;

    public Long getAvaliacaoId() {
        return avaliacaoId;
    }

    public void setAvaliacaoId(Long avaliacaoId) {
        this.avaliacaoId = avaliacaoId;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public TipoQuestao getTipo() {
        return tipo;
    }

    public void setTipo(TipoQuestao tipo) {
        this.tipo = tipo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public BigDecimal getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(BigDecimal pontuacao) {
        this.pontuacao = pontuacao;
    }
}
