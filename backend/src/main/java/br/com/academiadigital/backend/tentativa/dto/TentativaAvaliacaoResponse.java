package br.com.academiadigital.backend.tentativa.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.com.academiadigital.backend.tentativa.StatusTentativa;

public class TentativaAvaliacaoResponse {

    private Long id;
    private Long avaliacaoId;
    private String avaliacaoTitulo;
    private Long cursoId;
    private String cursoTitulo;
    private Long matriculaId;
    private Long alunoId;
    private String alunoNome;
    private Integer numeroTentativa;
    private StatusTentativa status;
    private LocalDateTime dataInicio;
    private LocalDateTime dataLimite;
    private LocalDateTime dataEnvio;
    private LocalDateTime dataFinalizacao;
    private BigDecimal pontuacaoTotal;
    private BigDecimal pontuacaoObtida;
    private BigDecimal nota;
    private Boolean aprovado;
    private List<QuestaoTentativaResponse> questoes =
            new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAvaliacaoId() {
        return avaliacaoId;
    }

    public void setAvaliacaoId(Long avaliacaoId) {
        this.avaliacaoId = avaliacaoId;
    }

    public String getAvaliacaoTitulo() {
        return avaliacaoTitulo;
    }

    public void setAvaliacaoTitulo(String avaliacaoTitulo) {
        this.avaliacaoTitulo = avaliacaoTitulo;
    }

    public Long getCursoId() {
        return cursoId;
    }

    public void setCursoId(Long cursoId) {
        this.cursoId = cursoId;
    }

    public String getCursoTitulo() {
        return cursoTitulo;
    }

    public void setCursoTitulo(String cursoTitulo) {
        this.cursoTitulo = cursoTitulo;
    }

    public Long getMatriculaId() {
        return matriculaId;
    }

    public void setMatriculaId(Long matriculaId) {
        this.matriculaId = matriculaId;
    }

    public Long getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(Long alunoId) {
        this.alunoId = alunoId;
    }

    public String getAlunoNome() {
        return alunoNome;
    }

    public void setAlunoNome(String alunoNome) {
        this.alunoNome = alunoNome;
    }

    public Integer getNumeroTentativa() {
        return numeroTentativa;
    }

    public void setNumeroTentativa(Integer numeroTentativa) {
        this.numeroTentativa = numeroTentativa;
    }

    public StatusTentativa getStatus() {
        return status;
    }

    public void setStatus(StatusTentativa status) {
        this.status = status;
    }

    public LocalDateTime getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDateTime dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDateTime getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(LocalDateTime dataLimite) {
        this.dataLimite = dataLimite;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public LocalDateTime getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(LocalDateTime dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public BigDecimal getPontuacaoTotal() {
        return pontuacaoTotal;
    }

    public void setPontuacaoTotal(BigDecimal pontuacaoTotal) {
        this.pontuacaoTotal = pontuacaoTotal;
    }

    public BigDecimal getPontuacaoObtida() {
        return pontuacaoObtida;
    }

    public void setPontuacaoObtida(BigDecimal pontuacaoObtida) {
        this.pontuacaoObtida = pontuacaoObtida;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public Boolean getAprovado() {
        return aprovado;
    }

    public void setAprovado(Boolean aprovado) {
        this.aprovado = aprovado;
    }

    public List<QuestaoTentativaResponse> getQuestoes() {
        return questoes;
    }

    public void setQuestoes(List<QuestaoTentativaResponse> questoes) {
        this.questoes = questoes;
    }
}
