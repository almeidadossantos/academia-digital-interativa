package br.com.academiadigital.backend.trilha.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import br.com.academiadigital.backend.trilha.StatusTrilha;
import br.com.academiadigital.backend.trilha.Trilha;
import br.com.academiadigital.backend.trilha.TrilhaCurso;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoResponse;
import br.com.academiadigital.backend.trilha.dto.TrilhaRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaResponse;

@Component
public class TrilhaMapper {

    private final TrilhaCursoMapper trilhaCursoMapper;

    public TrilhaMapper(
            TrilhaCursoMapper trilhaCursoMapper) {

        this.trilhaCursoMapper = trilhaCursoMapper;
    }

    public Trilha toEntity(
            TrilhaRequest request) {

        Trilha trilha = new Trilha();

        trilha.setTitulo(request.getTitulo());
        trilha.setDescricao(request.getDescricao());

        if (request.getStatus() == null) {
            trilha.setStatus(StatusTrilha.RASCUNHO);
        } else {
            trilha.setStatus(request.getStatus());
        }

        return trilha;
    }

    public void atualizarEntity(
            Trilha trilha,
            TrilhaRequest request) {

        trilha.setTitulo(request.getTitulo());
        trilha.setDescricao(request.getDescricao());

        if (request.getStatus() != null) {
            trilha.setStatus(request.getStatus());
        }
    }

    public TrilhaResponse toResponse(
            Trilha trilha,
            List<TrilhaCurso> cursos) {

        TrilhaResponse response =
                new TrilhaResponse();

        response.setId(trilha.getId());
        response.setTitulo(trilha.getTitulo());
        response.setDescricao(trilha.getDescricao());
        response.setStatus(trilha.getStatus());

        response.setDataCriacao(
                trilha.getDataCriacao()
        );

        response.setDataAtualizacao(
                trilha.getDataAtualizacao()
        );

        List<TrilhaCursoResponse> cursosResponse =
                cursos.stream()
                        .map(trilhaCursoMapper::toResponse)
                        .toList();

        response.setCursos(cursosResponse);

        return response;
    }
}