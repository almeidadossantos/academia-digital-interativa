package br.com.academiadigital.backend.trilha.mapper;

import org.springframework.stereotype.Component;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.trilha.Trilha;
import br.com.academiadigital.backend.trilha.TrilhaCurso;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoResponse;

@Component
public class TrilhaCursoMapper {

    public TrilhaCurso toEntity(
            TrilhaCursoRequest request,
            Trilha trilha,
            Curso curso) {

        TrilhaCurso trilhaCurso =
                new TrilhaCurso();

        trilhaCurso.setTrilha(trilha);
        trilhaCurso.setCurso(curso);
        trilhaCurso.setOrdem(request.getOrdem());

        return trilhaCurso;
    }

    public TrilhaCursoResponse toResponse(
            TrilhaCurso trilhaCurso) {

        TrilhaCursoResponse response =
                new TrilhaCursoResponse();

        response.setId(trilhaCurso.getId());

        response.setCursoId(
                trilhaCurso.getCurso().getId()
        );

        response.setCursoTitulo(
                trilhaCurso.getCurso().getTitulo()
        );

        response.setOrdem(
                trilhaCurso.getOrdem()
        );

        return response;
    }
}