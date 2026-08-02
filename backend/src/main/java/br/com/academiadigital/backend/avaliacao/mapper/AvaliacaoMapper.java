package br.com.academiadigital.backend.avaliacao.mapper;

import org.springframework.stereotype.Component;

import br.com.academiadigital.backend.avaliacao.Avaliacao;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoUpdateRequest;
import br.com.academiadigital.backend.curso.Curso;

@Component
public class AvaliacaoMapper {

    public Avaliacao toEntity(
            AvaliacaoRequest request,
            Curso curso
    ) {
        Avaliacao avaliacao = new Avaliacao();

        avaliacao.setCurso(curso);
        avaliacao.setTitulo(request.getTitulo().trim());
        avaliacao.setDescricao(request.getDescricao().trim());
        avaliacao.setOrdem(request.getOrdem());
        avaliacao.setNotaMinima(request.getNotaMinima());
        avaliacao.setMaximoTentativas(
                request.getMaximoTentativas()
        );
        avaliacao.setTempoLimiteMinutos(
                request.getTempoLimiteMinutos()
        );

        if (request.getStatus() != null) {
            avaliacao.setStatus(request.getStatus());
        }

        return avaliacao;
    }

    public void updateEntity(
            Avaliacao avaliacao,
            AvaliacaoUpdateRequest request,
            Curso curso
    ) {
        avaliacao.setCurso(curso);
        avaliacao.setTitulo(request.getTitulo().trim());
        avaliacao.setDescricao(request.getDescricao().trim());
        avaliacao.setOrdem(request.getOrdem());
        avaliacao.setNotaMinima(request.getNotaMinima());
        avaliacao.setMaximoTentativas(
                request.getMaximoTentativas()
        );
        avaliacao.setTempoLimiteMinutos(
                request.getTempoLimiteMinutos()
        );

        if (request.getStatus() != null) {
            avaliacao.setStatus(request.getStatus());
        }
    }

    public AvaliacaoResponse toResponse(
            Avaliacao avaliacao
    ) {
        AvaliacaoResponse response =
                new AvaliacaoResponse();

        response.setId(avaliacao.getId());
        response.setCursoId(
                avaliacao.getCurso().getId()
        );
        response.setCursoTitulo(
                avaliacao.getCurso().getTitulo()
        );
        response.setTitulo(avaliacao.getTitulo());
        response.setDescricao(avaliacao.getDescricao());
        response.setOrdem(avaliacao.getOrdem());
        response.setNotaMinima(
                avaliacao.getNotaMinima()
        );
        response.setMaximoTentativas(
                avaliacao.getMaximoTentativas()
        );
        response.setTempoLimiteMinutos(
                avaliacao.getTempoLimiteMinutos()
        );
        response.setStatus(avaliacao.getStatus());
        response.setDataCriacao(
                avaliacao.getDataCriacao()
        );
        response.setDataAtualizacao(
                avaliacao.getDataAtualizacao()
        );

        return response;
    }
}
