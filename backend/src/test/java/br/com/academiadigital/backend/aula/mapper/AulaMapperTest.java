package br.com.academiadigital.backend.aula.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.aula.Aula;
import br.com.academiadigital.backend.aula.StatusAula;
import br.com.academiadigital.backend.aula.dto.AulaRequest;
import br.com.academiadigital.backend.aula.dto.AulaResponse;
import br.com.academiadigital.backend.aula.dto.AulaUpdateRequest;
import br.com.academiadigital.backend.curso.Curso;

class AulaMapperTest {

    private final AulaMapper aulaMapper = new AulaMapper();

    @Test
    void deveConverterRequestParaEntidade() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        AulaRequest request = new AulaRequest();

        request.setCursoId(1L);
        request.setTitulo("  Introdução ao computador  ");
        request.setDescricao(
                "  Conhecendo os componentes do computador.  "
        );
        request.setOrdem(1);
        request.setDuracaoMinutos(30);
        request.setVideoUrl(
                "  https://exemplo.com/videos/aula-1  "
        );
        request.setStatus(StatusAula.PUBLICADA);

        Aula aula = aulaMapper.toEntity(request, curso);

        assertSame(curso, aula.getCurso());
        assertEquals(
                "Introdução ao computador",
                aula.getTitulo()
        );
        assertEquals(
                "Conhecendo os componentes do computador.",
                aula.getDescricao()
        );
        assertEquals(1, aula.getOrdem());
        assertEquals(30, aula.getDuracaoMinutos());
        assertEquals(
                "https://exemplo.com/videos/aula-1",
                aula.getVideoUrl()
        );
        assertEquals(
                StatusAula.PUBLICADA,
                aula.getStatus()
        );
    }

    @Test
    void deveConverterVideoEmBrancoParaNulo() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        AulaRequest request = new AulaRequest();

        request.setCursoId(1L);
        request.setTitulo("Aula sem vídeo");
        request.setDescricao("Conteúdo textual.");
        request.setOrdem(2);
        request.setDuracaoMinutos(20);
        request.setVideoUrl("   ");
        request.setStatus(null);

        Aula aula = aulaMapper.toEntity(request, curso);

        assertNull(aula.getVideoUrl());
        assertNull(aula.getStatus());
    }

    @Test
    void deveAtualizarEntidadeComDadosDoRequest() {
        Curso cursoOriginal = criarCurso(
                1L,
                "Curso original"
        );

        Curso novoCurso = criarCurso(
                2L,
                "Novo curso"
        );

        Aula aula = new Aula();

        aula.setCurso(cursoOriginal);
        aula.setTitulo("Título original");
        aula.setDescricao("Descrição original.");
        aula.setOrdem(1);
        aula.setDuracaoMinutos(10);
        aula.setVideoUrl("https://exemplo.com/original");
        aula.setStatus(StatusAula.RASCUNHO);

        AulaUpdateRequest request = new AulaUpdateRequest();

        request.setCursoId(2L);
        request.setTitulo("  Título atualizado  ");
        request.setDescricao("  Descrição atualizada.  ");
        request.setOrdem(3);
        request.setDuracaoMinutos(45);
        request.setVideoUrl(
                "  https://exemplo.com/atualizado  "
        );
        request.setStatus(StatusAula.PUBLICADA);

        aulaMapper.updateEntity(
                aula,
                request,
                novoCurso
        );

        assertSame(novoCurso, aula.getCurso());
        assertEquals(
                "Título atualizado",
                aula.getTitulo()
        );
        assertEquals(
                "Descrição atualizada.",
                aula.getDescricao()
        );
        assertEquals(3, aula.getOrdem());
        assertEquals(45, aula.getDuracaoMinutos());
        assertEquals(
                "https://exemplo.com/atualizado",
                aula.getVideoUrl()
        );
        assertEquals(
                StatusAula.PUBLICADA,
                aula.getStatus()
        );
    }

    @Test
    void devePreservarStatusQuandoAtualizacaoNaoInformarStatus() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Aula aula = new Aula();
        aula.setStatus(StatusAula.PUBLICADA);

        AulaUpdateRequest request = new AulaUpdateRequest();

        request.setCursoId(1L);
        request.setTitulo("Aula atualizada");
        request.setDescricao("Descrição atualizada.");
        request.setOrdem(1);
        request.setDuracaoMinutos(35);
        request.setVideoUrl(null);
        request.setStatus(null);

        aulaMapper.updateEntity(
                aula,
                request,
                curso
        );

        assertEquals(
                StatusAula.PUBLICADA,
                aula.getStatus()
        );
        assertNull(aula.getVideoUrl());
    }

    @Test
    void deveConverterEntidadeParaResponse() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Aula aula = new Aula();

        aula.setId(10L);
        aula.setCurso(curso);
        aula.setTitulo("Introdução ao computador");
        aula.setDescricao(
                "Conhecendo os componentes do computador."
        );
        aula.setOrdem(1);
        aula.setDuracaoMinutos(30);
        aula.setVideoUrl(
                "https://exemplo.com/videos/aula-1"
        );
        aula.setStatus(StatusAula.PUBLICADA);
        aula.prePersist();

        AulaResponse response = aulaMapper.toResponse(aula);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getCursoId());
        assertEquals(
                "Informática Básica",
                response.getCursoTitulo()
        );
        assertEquals(
                "Introdução ao computador",
                response.getTitulo()
        );
        assertEquals(
                "Conhecendo os componentes do computador.",
                response.getDescricao()
        );
        assertEquals(1, response.getOrdem());
        assertEquals(30, response.getDuracaoMinutos());
        assertEquals(
                "https://exemplo.com/videos/aula-1",
                response.getVideoUrl()
        );
        assertEquals(
                StatusAula.PUBLICADA,
                response.getStatus()
        );
        assertNotNull(response.getDataCriacao());
        assertNotNull(response.getDataAtualizacao());
    }

    private Curso criarCurso(
            Long id,
            String titulo) {

        Curso curso = new Curso();

        curso.setId(id);
        curso.setTitulo(titulo);

        return curso;
    }
}