package br.com.academiadigital.backend.tentativa.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import br.com.academiadigital.backend.avaliacao.TipoQuestao;

public class QuestaoTentativaResponse {

    private Long id;
    private String enunciado;
    private TipoQuestao tipo;
    private Integer ordem;
    private BigDecimal pontuacao;
    private List<AlternativaTentativaResponse> alternativas =
            new ArrayList<>();
    private RespostaQuestaoResponse resposta;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<AlternativaTentativaResponse> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(
            List<AlternativaTentativaResponse> alternativas) {

        this.alternativas = alternativas;
    }

    public RespostaQuestaoResponse getResposta() {
        return resposta;
    }

    public void setResposta(RespostaQuestaoResponse resposta) {
        this.resposta = resposta;
    }
}
