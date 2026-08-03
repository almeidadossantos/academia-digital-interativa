package br.com.academiadigital.backend.avaliacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class AlternativaRequest {

    @NotNull(message = "A questão é obrigatória.")
    @Positive(
            message = "O ID da questão deve ser maior que zero."
    )
    private Long questaoId;

    @NotBlank(message = "O texto da alternativa é obrigatório.")
    private String texto;

    @NotNull(
            message = "A indicação de alternativa correta é obrigatória."
    )
    private Boolean correta;

    @NotNull(message = "A ordem da alternativa é obrigatória.")
    @Positive(
            message = "A ordem da alternativa deve ser maior que zero."
    )
    private Integer ordem;

    public Long getQuestaoId() {
        return questaoId;
    }

    public void setQuestaoId(Long questaoId) {
        this.questaoId = questaoId;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Boolean getCorreta() {
        return correta;
    }

    public void setCorreta(Boolean correta) {
        this.correta = correta;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}
