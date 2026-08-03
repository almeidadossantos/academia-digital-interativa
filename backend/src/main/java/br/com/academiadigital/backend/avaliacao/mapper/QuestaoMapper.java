package br.com.academiadigital.backend.avaliacao.mapper;

import org.springframework.stereotype.Component;

import br.com.academiadigital.backend.avaliacao.Avaliacao;
import br.com.academiadigital.backend.avaliacao.Questao;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoUpdateRequest;

@Component
public class QuestaoMapper {

    public Questao toEntity(
            QuestaoRequest request,
            Avaliacao avaliacao
    ) {
        Questao questao = new Questao();

        questao.setAvaliacao(avaliacao);
        questao.setEnunciado(
                request.getEnunciado().trim()
        );
        questao.setTipo(request.getTipo());
        questao.setOrdem(request.getOrdem());
        questao.setPontuacao(
                request.getPontuacao()
        );

        return questao;
    }

    public void updateEntity(
            Questao questao,
            QuestaoUpdateRequest request,
            Avaliacao avaliacao
    ) {
        questao.setAvaliacao(avaliacao);
        questao.setEnunciado(
                request.getEnunciado().trim()
        );
        questao.setTipo(request.getTipo());
        questao.setOrdem(request.getOrdem());
        questao.setPontuacao(
                request.getPontuacao()
        );
    }

    public QuestaoResponse toResponse(
            Questao questao
    ) {
        QuestaoResponse response =
                new QuestaoResponse();

        response.setId(questao.getId());
        response.setAvaliacaoId(
                questao.getAvaliacao().getId()
        );
        response.setAvaliacaoTitulo(
                questao.getAvaliacao().getTitulo()
        );
        response.setEnunciado(
                questao.getEnunciado()
        );
        response.setTipo(questao.getTipo());
        response.setOrdem(questao.getOrdem());
        response.setPontuacao(
                questao.getPontuacao()
        );
        response.setDataCriacao(
                questao.getDataCriacao()
        );
        response.setDataAtualizacao(
                questao.getDataAtualizacao()
        );

        return response;
    }
}
