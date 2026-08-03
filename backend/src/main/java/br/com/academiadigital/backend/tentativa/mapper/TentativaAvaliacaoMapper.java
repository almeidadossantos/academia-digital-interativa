package br.com.academiadigital.backend.tentativa.mapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.academiadigital.backend.avaliacao.Alternativa;
import br.com.academiadigital.backend.avaliacao.AlternativaRepository;
import br.com.academiadigital.backend.avaliacao.Questao;
import br.com.academiadigital.backend.avaliacao.QuestaoRepository;
import br.com.academiadigital.backend.tentativa.RespostaQuestao;
import br.com.academiadigital.backend.tentativa.RespostaQuestaoRepository;
import br.com.academiadigital.backend.tentativa.TentativaAvaliacao;
import br.com.academiadigital.backend.tentativa.dto.AlternativaTentativaResponse;
import br.com.academiadigital.backend.tentativa.dto.QuestaoTentativaResponse;
import br.com.academiadigital.backend.tentativa.dto.RespostaQuestaoResponse;
import br.com.academiadigital.backend.tentativa.dto.TentativaAvaliacaoResponse;

@Component
public class TentativaAvaliacaoMapper {

    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final RespostaQuestaoRepository respostaQuestaoRepository;

    public TentativaAvaliacaoMapper(
            QuestaoRepository questaoRepository,
            AlternativaRepository alternativaRepository,
            RespostaQuestaoRepository respostaQuestaoRepository) {

        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
        this.respostaQuestaoRepository = respostaQuestaoRepository;
    }

    public TentativaAvaliacaoResponse toResponse(
            TentativaAvaliacao tentativa) {

        TentativaAvaliacaoResponse response =
                new TentativaAvaliacaoResponse();

        response.setId(tentativa.getId());
        response.setAvaliacaoId(
                tentativa.getAvaliacao().getId()
        );
        response.setAvaliacaoTitulo(
                tentativa.getAvaliacao().getTitulo()
        );
        response.setCursoId(
                tentativa.getAvaliacao().getCurso().getId()
        );
        response.setCursoTitulo(
                tentativa.getAvaliacao().getCurso().getTitulo()
        );
        response.setMatriculaId(
                tentativa.getMatricula().getId()
        );
        response.setAlunoId(
                tentativa.getMatricula().getAluno().getId()
        );
        response.setAlunoNome(
                tentativa.getMatricula().getAluno().getNome()
        );
        response.setNumeroTentativa(
                tentativa.getNumeroTentativa()
        );
        response.setStatus(tentativa.getStatus());
        response.setDataInicio(tentativa.getDataInicio());
        response.setDataLimite(tentativa.getDataLimite());
        response.setDataEnvio(tentativa.getDataEnvio());
        response.setDataFinalizacao(
                tentativa.getDataFinalizacao()
        );
        response.setPontuacaoTotal(
                tentativa.getPontuacaoTotal()
        );
        response.setPontuacaoObtida(
                tentativa.getPontuacaoObtida()
        );
        response.setNota(tentativa.getNota());
        response.setAprovado(tentativa.getAprovado());
        response.setQuestoes(mapearQuestoes(tentativa));

        return response;
    }

    public RespostaQuestaoResponse toRespostaResponse(
            RespostaQuestao resposta) {

        RespostaQuestaoResponse response =
                new RespostaQuestaoResponse();

        response.setId(resposta.getId());
        response.setQuestaoId(resposta.getQuestao().getId());

        if (resposta.getAlternativaSelecionada() != null) {
            response.setAlternativaSelecionadaId(
                    resposta.getAlternativaSelecionada().getId()
            );
        }

        response.setRespostaTexto(resposta.getRespostaTexto());
        response.setCorrigida(resposta.getCorrigida());
        response.setCorreta(resposta.getCorreta());
        response.setPontuacaoObtida(
                resposta.getPontuacaoObtida()
        );
        response.setFeedback(resposta.getFeedback());

        return response;
    }

    private List<QuestaoTentativaResponse> mapearQuestoes(
            TentativaAvaliacao tentativa) {

        List<Questao> questoes = questaoRepository
                .findAllByAvaliacaoIdOrderByOrdemAsc(
                        tentativa.getAvaliacao().getId()
                );

        Map<Long, RespostaQuestao> respostasPorQuestao =
                respostaQuestaoRepository
                        .findAllByTentativaIdOrderByQuestaoOrdemAsc(
                                tentativa.getId()
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        resposta ->
                                                resposta.getQuestao().getId(),
                                        Function.identity()
                                )
                        );

        return questoes.stream()
                .map(questao -> mapearQuestao(
                        questao,
                        respostasPorQuestao.get(questao.getId())
                ))
                .toList();
    }

    private QuestaoTentativaResponse mapearQuestao(
            Questao questao,
            RespostaQuestao resposta) {

        QuestaoTentativaResponse response =
                new QuestaoTentativaResponse();

        response.setId(questao.getId());
        response.setEnunciado(questao.getEnunciado());
        response.setTipo(questao.getTipo());
        response.setOrdem(questao.getOrdem());
        response.setPontuacao(questao.getPontuacao());
        response.setAlternativas(
                alternativaRepository
                        .findAllByQuestaoIdOrderByOrdemAsc(
                                questao.getId()
                        )
                        .stream()
                        .map(this::mapearAlternativa)
                        .toList()
        );

        if (resposta != null) {
            response.setResposta(toRespostaResponse(resposta));
        }

        return response;
    }

    private AlternativaTentativaResponse mapearAlternativa(
            Alternativa alternativa) {

        AlternativaTentativaResponse response =
                new AlternativaTentativaResponse();

        response.setId(alternativa.getId());
        response.setTexto(alternativa.getTexto());
        response.setOrdem(alternativa.getOrdem());

        return response;
    }
}
