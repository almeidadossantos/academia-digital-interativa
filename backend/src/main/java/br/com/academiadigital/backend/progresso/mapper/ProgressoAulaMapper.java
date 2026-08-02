package br.com.academiadigital.backend.progresso.mapper;

import org.springframework.stereotype.Component;

import br.com.academiadigital.backend.progresso.ProgressoAula;
import br.com.academiadigital.backend.progresso.dto.ProgressoAulaResponse;

@Component
public class ProgressoAulaMapper {

    public ProgressoAulaResponse toResponse(
            ProgressoAula progresso) {

        ProgressoAulaResponse response =
                new ProgressoAulaResponse();

        response.setId(
                progresso.getId()
        );

        response.setMatriculaId(
                progresso.getMatricula().getId()
        );

        response.setCursoId(
                progresso.getMatricula()
                        .getCurso()
                        .getId()
        );

        response.setCursoTitulo(
                progresso.getMatricula()
                        .getCurso()
                        .getTitulo()
        );

        response.setAulaId(
                progresso.getAula().getId()
        );

        response.setAulaTitulo(
                progresso.getAula().getTitulo()
        );

        response.setAulaOrdem(
                progresso.getAula().getOrdem()
        );

        response.setConcluida(
                progresso.getConcluida()
        );

        response.setDataConclusao(
                progresso.getDataConclusao()
        );

        response.setDataCriacao(
                progresso.getDataCriacao()
        );

        response.setDataAtualizacao(
                progresso.getDataAtualizacao()
        );

        return response;
    }
}