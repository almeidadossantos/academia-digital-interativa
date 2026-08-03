package br.com.academiadigital.backend.avaliacao.mapper;

import org.springframework.stereotype.Component;

import br.com.academiadigital.backend.avaliacao.Alternativa;
import br.com.academiadigital.backend.avaliacao.Questao;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaRequest;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaResponse;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaUpdateRequest;

@Component
public class AlternativaMapper {

    public Alternativa toEntity(
            AlternativaRequest request,
            Questao questao
    ) {
        Alternativa alternativa =
                new Alternativa();

        alternativa.setQuestao(questao);
        alternativa.setTexto(
                request.getTexto().trim()
        );
        alternativa.setCorreta(
                request.getCorreta()
        );
        alternativa.setOrdem(
                request.getOrdem()
        );

        return alternativa;
    }

    public void updateEntity(
            Alternativa alternativa,
            AlternativaUpdateRequest request,
            Questao questao
    ) {
        alternativa.setQuestao(questao);
        alternativa.setTexto(
                request.getTexto().trim()
        );
        alternativa.setCorreta(
                request.getCorreta()
        );
        alternativa.setOrdem(
                request.getOrdem()
        );
    }

    public AlternativaResponse toResponse(
            Alternativa alternativa
    ) {
        AlternativaResponse response =
                new AlternativaResponse();

        response.setId(
                alternativa.getId()
        );

        response.setQuestaoId(
                alternativa.getQuestao().getId()
        );

        response.setQuestaoEnunciado(
                alternativa.getQuestao().getEnunciado()
        );

        response.setTexto(
                alternativa.getTexto()
        );

        response.setCorreta(
                alternativa.getCorreta()
        );

        response.setOrdem(
                alternativa.getOrdem()
        );

        response.setDataCriacao(
                alternativa.getDataCriacao()
        );

        response.setDataAtualizacao(
                alternativa.getDataAtualizacao()
        );

        return response;
    }
}
