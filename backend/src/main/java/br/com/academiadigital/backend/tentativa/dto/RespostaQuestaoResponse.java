package br.com.academiadigital.backend.tentativa.dto;

import java.math.BigDecimal;

public class RespostaQuestaoResponse {

    private Long id;
    private Long questaoId;
    private Long alternativaSelecionadaId;
    private String respostaTexto;
    private Boolean corrigida;
    private Boolean correta;
    private BigDecimal pontuacaoObtida;
    private String feedback;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestaoId() {
        return questaoId;
    }

    public void setQuestaoId(Long questaoId) {
        this.questaoId = questaoId;
    }

    public Long getAlternativaSelecionadaId() {
        return alternativaSelecionadaId;
    }

    public void setAlternativaSelecionadaId(Long alternativaSelecionadaId) {
        this.alternativaSelecionadaId = alternativaSelecionadaId;
    }

    public String getRespostaTexto() {
        return respostaTexto;
    }

    public void setRespostaTexto(String respostaTexto) {
        this.respostaTexto = respostaTexto;
    }

    public Boolean getCorrigida() {
        return corrigida;
    }

    public void setCorrigida(Boolean corrigida) {
        this.corrigida = corrigida;
    }

    public Boolean getCorreta() {
        return correta;
    }

    public void setCorreta(Boolean correta) {
        this.correta = correta;
    }

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
