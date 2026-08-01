package br.com.academiadigital.backend.curso.mapper;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.dto.CursoRequest;
import br.com.academiadigital.backend.curso.dto.CursoResponse;
import br.com.academiadigital.backend.curso.dto.CursoUpdateRequest;

public final class CursoMapper {

    private CursoMapper() {
    }

    public static Curso toEntity(CursoRequest request) {
        Curso curso = new Curso();

        curso.setTitulo(request.getTitulo());
        curso.setDescricao(request.getDescricao());
        curso.setCargaHoraria(request.getCargaHoraria());
        curso.setNivel(request.getNivel());
        curso.setStatus(request.getStatus());
        curso.setImagemUrl(request.getImagemUrl());

        return curso;
    }

    public static void updateEntity(
            Curso curso,
            CursoUpdateRequest request) {

        curso.setTitulo(request.getTitulo());
        curso.setDescricao(request.getDescricao());
        curso.setCargaHoraria(request.getCargaHoraria());
        curso.setNivel(request.getNivel());

        if (request.getStatus() != null) {
            curso.setStatus(request.getStatus());
        }

        curso.setImagemUrl(request.getImagemUrl());
    }

    public static CursoResponse toResponse(Curso curso) {
        CursoResponse response = new CursoResponse();

        response.setId(curso.getId());
        response.setTitulo(curso.getTitulo());
        response.setDescricao(curso.getDescricao());
        response.setCargaHoraria(curso.getCargaHoraria());
        response.setNivel(curso.getNivel());
        response.setStatus(curso.getStatus());
        response.setImagemUrl(curso.getImagemUrl());
        response.setDataCriacao(curso.getDataCriacao());
        response.setDataAtualizacao(curso.getDataAtualizacao());

        return response;
    }
}