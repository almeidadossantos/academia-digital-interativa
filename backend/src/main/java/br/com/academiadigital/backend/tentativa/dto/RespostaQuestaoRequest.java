package br.com.academiadigital.backend.tentativa.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class RespostaQuestaoRequest {

    @Positive(message = "O ID da alternativa deve ser maior que zero.")
    private Long alternativaId;

    @Size(
            max = 10000,
            message = "A resposta textual deve possuir no máximo 10000 caracteres."
    )
    private String respostaTexto;

    public Long getAlternativaId() {
        return alternativaId;
    }

    public void setAlternativaId(Long alternativaId) {
        this.alternativaId = alternativaId;
    }

    public String getRespostaTexto() {
        return respostaTexto;
    }

    public void setRespostaTexto(String respostaTexto) {
        this.respostaTexto = respostaTexto;
    }
}
