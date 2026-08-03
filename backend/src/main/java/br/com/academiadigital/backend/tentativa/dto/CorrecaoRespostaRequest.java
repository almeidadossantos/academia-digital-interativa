package br.com.academiadigital.backend.tentativa.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CorrecaoRespostaRequest {

    @NotNull(message = "A pontuação obtida é obrigatória.")
    @DecimalMin(
            value = "0.00",
            message = "A pontuação obtida não pode ser negativa."
    )
    @Digits(
            integer = 5,
            fraction = 2,
            message = "A pontuação obtida deve possuir no máximo duas casas decimais."
    )
    private BigDecimal pontuacaoObtida;

    @Size(
            max = 5000,
            message = "O feedback deve possuir no máximo 5000 caracteres."
    )
    private String feedback;

    public BigDecimal getPontuacaoObtida() {
        return pontuacaoObtida;
    }

    public void setPontuacaoObtida(BigDecimal pontuacaoObtida) {
        this.pontuacaoObtida = pontuacaoObtida;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
