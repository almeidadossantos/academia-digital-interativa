package br.com.academiadigital.backend.curso.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.NivelCurso;
import br.com.academiadigital.backend.curso.StatusCurso;
import br.com.academiadigital.backend.curso.dto.CursoRequest;
import br.com.academiadigital.backend.curso.dto.CursoResponse;
import br.com.academiadigital.backend.curso.dto.CursoUpdateRequest;

class CursoMapperTest {

    @Test
    void deveConverterRequestParaEntidade() {
        CursoRequest request = new CursoRequest();
        request.setTitulo("Java e Spring Boot");
        request.setDescricao("Curso completo de desenvolvimento backend.");
        request.setCargaHoraria(60);
        request.setNivel(NivelCurso.INTERMEDIARIO);
        request.setStatus(StatusCurso.PUBLICADO);
        request.setImagemUrl("https://exemplo.com/spring.png");

        Curso curso = CursoMapper.toEntity(request);

        assertNull(curso.getId());
        assertEquals("Java e Spring Boot", curso.getTitulo());
        assertEquals(
                "Curso completo de desenvolvimento backend.",
                curso.getDescricao()
        );
        assertEquals(60, curso.getCargaHoraria());
        assertEquals(NivelCurso.INTERMEDIARIO, curso.getNivel());
        assertEquals(StatusCurso.PUBLICADO, curso.getStatus());
        assertEquals(
                "https://exemplo.com/spring.png",
                curso.getImagemUrl()
        );
    }

    @Test
    void deveAtualizarEntidadeComStatusInformado() {
        Curso curso = criarCursoExistente();

        CursoUpdateRequest request = new CursoUpdateRequest();
        request.setTitulo("Spring Boot avançado");
        request.setDescricao("Conteúdo atualizado.");
        request.setCargaHoraria(80);
        request.setNivel(NivelCurso.AVANCADO);
        request.setStatus(StatusCurso.INATIVO);
        request.setImagemUrl("https://exemplo.com/avancado.png");

        CursoMapper.updateEntity(curso, request);

        assertEquals("Spring Boot avançado", curso.getTitulo());
        assertEquals("Conteúdo atualizado.", curso.getDescricao());
        assertEquals(80, curso.getCargaHoraria());
        assertEquals(NivelCurso.AVANCADO, curso.getNivel());
        assertEquals(StatusCurso.INATIVO, curso.getStatus());
        assertEquals(
                "https://exemplo.com/avancado.png",
                curso.getImagemUrl()
        );
    }

    @Test
    void devePreservarStatusQuandoNaoForInformadoNaAtualizacao() {
        Curso curso = criarCursoExistente();

        CursoUpdateRequest request = new CursoUpdateRequest();
        request.setTitulo("Spring Boot atualizado");
        request.setDescricao("Nova descrição.");
        request.setCargaHoraria(50);
        request.setNivel(NivelCurso.INTERMEDIARIO);
        request.setStatus(null);
        request.setImagemUrl(null);

        CursoMapper.updateEntity(curso, request);

        assertEquals(StatusCurso.PUBLICADO, curso.getStatus());
        assertNull(curso.getImagemUrl());
    }

    @Test
    void deveConverterEntidadeParaResponse() {
        Curso curso = criarCursoExistente();
        curso.setId(10L);
        curso.prePersist();

        CursoResponse response = CursoMapper.toResponse(curso);

        assertEquals(10L, response.getId());
        assertEquals("Curso existente", response.getTitulo());
        assertEquals("Descrição existente.", response.getDescricao());
        assertEquals(40, response.getCargaHoraria());
        assertEquals(NivelCurso.INICIANTE, response.getNivel());
        assertEquals(StatusCurso.PUBLICADO, response.getStatus());
        assertEquals(
                "https://exemplo.com/curso.png",
                response.getImagemUrl()
        );
        assertNotNull(response.getDataCriacao());
        assertNotNull(response.getDataAtualizacao());
    }

    private Curso criarCursoExistente() {
        Curso curso = new Curso();
        curso.setTitulo("Curso existente");
        curso.setDescricao("Descrição existente.");
        curso.setCargaHoraria(40);
        curso.setNivel(NivelCurso.INICIANTE);
        curso.setStatus(StatusCurso.PUBLICADO);
        curso.setImagemUrl("https://exemplo.com/curso.png");
        return curso;
    }
}