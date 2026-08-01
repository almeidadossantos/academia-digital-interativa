package br.com.academiadigital.backend.aula.mapper;

import org.springframework.stereotype.Component;

import br.com.academiadigital.backend.aula.Aula;
import br.com.academiadigital.backend.aula.dto.AulaRequest;
import br.com.academiadigital.backend.aula.dto.AulaResponse;
import br.com.academiadigital.backend.aula.dto.AulaUpdateRequest;
import br.com.academiadigital.backend.curso.Curso;

@Component
public class AulaMapper {

    public Aula toEntity(
            AulaRequest request,
            Curso curso
    ) {
        Aula aula = new Aula();

        aula.setCurso(curso);
        aula.setTitulo(request.getTitulo().trim());
        aula.setDescricao(request.getDescricao().trim());
        aula.setOrdem(request.getOrdem());
        aula.setDuracaoMinutos(request.getDuracaoMinutos());
        aula.setVideoUrl(normalizarTextoOpcional(request.getVideoUrl()));

        if (request.getStatus() != null) {
            aula.setStatus(request.getStatus());
        }

        return aula;
    }

    public void updateEntity(
            Aula aula,
            AulaUpdateRequest request,
            Curso curso
    ) {
        aula.setCurso(curso);
        aula.setTitulo(request.getTitulo().trim());
        aula.setDescricao(request.getDescricao().trim());
        aula.setOrdem(request.getOrdem());
        aula.setDuracaoMinutos(request.getDuracaoMinutos());
        aula.setVideoUrl(normalizarTextoOpcional(request.getVideoUrl()));

        if (request.getStatus() != null) {
            aula.setStatus(request.getStatus());
        }
    }

    public AulaResponse toResponse(Aula aula) {
        AulaResponse response = new AulaResponse();

        response.setId(aula.getId());
        response.setCursoId(aula.getCurso().getId());
        response.setCursoTitulo(aula.getCurso().getTitulo());
        response.setTitulo(aula.getTitulo());
        response.setDescricao(aula.getDescricao());
        response.setOrdem(aula.getOrdem());
        response.setDuracaoMinutos(aula.getDuracaoMinutos());
        response.setVideoUrl(aula.getVideoUrl());
        response.setStatus(aula.getStatus());
        response.setDataCriacao(aula.getDataCriacao());
        response.setDataAtualizacao(aula.getDataAtualizacao());

        return response;
    }

    private String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}