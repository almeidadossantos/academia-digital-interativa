package br.com.academiadigital.backend.avaliacao.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.avaliacao.Avaliacao;
import br.com.academiadigital.backend.avaliacao.StatusAvaliacao;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoUpdateRequest;
import br.com.academiadigital.backend.curso.Curso;

class AvaliacaoMapperTest {

    private final AvaliacaoMapper avaliacaoMapper =
            new AvaliacaoMapper();

    @Test
    void deveConverterRequestParaEntidade() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        AvaliacaoRequest request =
                new AvaliacaoRequest();

        request.setCursoId(1L);
        request.setTitulo(
                "  Avaliação de conhecimentos básicos  "
        );
        request.setDescricao(
                "  Avaliação dos conteúdos iniciais.  "
        );
        request.setOrdem(1);
        request.setNotaMinima(
                new BigDecimal("7.00")
        );
        request.setMaximoTentativas(3);
        request.setTempoLimiteMinutos(60);
        request.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        Avaliacao avaliacao =
                avaliacaoMapper.toEntity(
                        request,
                        curso
                );

        assertSame(curso, avaliacao.getCurso());

        assertEquals(
                "Avaliação de conhecimentos básicos",
                avaliacao.getTitulo()
        );

        assertEquals(
                "Avaliação dos conteúdos iniciais.",
                avaliacao.getDescricao()
        );

        assertEquals(1, avaliacao.getOrdem());

        assertEquals(
                new BigDecimal("7.00"),
                avaliacao.getNotaMinima()
        );

        assertEquals(
                3,
                avaliacao.getMaximoTentativas()
        );

        assertEquals(
                60,
                avaliacao.getTempoLimiteMinutos()
        );

        assertEquals(
                StatusAvaliacao.PUBLICADA,
                avaliacao.getStatus()
        );
    }

    @Test
    void devePreservarValoresOpcionaisNulosNaCriacao() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        AvaliacaoRequest request =
                new AvaliacaoRequest();

        request.setCursoId(1L);
        request.setTitulo("Avaliação sem limite");
        request.setDescricao(
                "Avaliação sem limite de tempo."
        );
        request.setOrdem(2);
        request.setNotaMinima(
                new BigDecimal("6.00")
        );
        request.setMaximoTentativas(2);
        request.setTempoLimiteMinutos(null);
        request.setStatus(null);

        Avaliacao avaliacao =
                avaliacaoMapper.toEntity(
                        request,
                        curso
                );

        assertNull(
                avaliacao.getTempoLimiteMinutos()
        );

        assertNull(avaliacao.getStatus());
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

        Avaliacao avaliacao =
                new Avaliacao();

        avaliacao.setCurso(cursoOriginal);
        avaliacao.setTitulo("Título original");
        avaliacao.setDescricao(
                "Descrição original."
        );
        avaliacao.setOrdem(1);
        avaliacao.setNotaMinima(
                new BigDecimal("6.00")
        );
        avaliacao.setMaximoTentativas(2);
        avaliacao.setTempoLimiteMinutos(30);
        avaliacao.setStatus(
                StatusAvaliacao.RASCUNHO
        );

        AvaliacaoUpdateRequest request =
                new AvaliacaoUpdateRequest();

        request.setCursoId(2L);
        request.setTitulo(
                "  Título atualizado  "
        );
        request.setDescricao(
                "  Descrição atualizada.  "
        );
        request.setOrdem(3);
        request.setNotaMinima(
                new BigDecimal("8.00")
        );
        request.setMaximoTentativas(4);
        request.setTempoLimiteMinutos(90);
        request.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        avaliacaoMapper.updateEntity(
                avaliacao,
                request,
                novoCurso
        );

        assertSame(
                novoCurso,
                avaliacao.getCurso()
        );

        assertEquals(
                "Título atualizado",
                avaliacao.getTitulo()
        );

        assertEquals(
                "Descrição atualizada.",
                avaliacao.getDescricao()
        );

        assertEquals(3, avaliacao.getOrdem());

        assertEquals(
                new BigDecimal("8.00"),
                avaliacao.getNotaMinima()
        );

        assertEquals(
                4,
                avaliacao.getMaximoTentativas()
        );

        assertEquals(
                90,
                avaliacao.getTempoLimiteMinutos()
        );

        assertEquals(
                StatusAvaliacao.PUBLICADA,
                avaliacao.getStatus()
        );
    }

    @Test
    void devePreservarStatusQuandoAtualizacaoNaoInformarStatus() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Avaliacao avaliacao =
                new Avaliacao();

        avaliacao.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        AvaliacaoUpdateRequest request =
                new AvaliacaoUpdateRequest();

        request.setCursoId(1L);
        request.setTitulo(
                "Avaliação atualizada"
        );
        request.setDescricao(
                "Descrição atualizada."
        );
        request.setOrdem(1);
        request.setNotaMinima(
                new BigDecimal("7.00")
        );
        request.setMaximoTentativas(3);
        request.setTempoLimiteMinutos(null);
        request.setStatus(null);

        avaliacaoMapper.updateEntity(
                avaliacao,
                request,
                curso
        );

        assertEquals(
                StatusAvaliacao.PUBLICADA,
                avaliacao.getStatus()
        );

        assertNull(
                avaliacao.getTempoLimiteMinutos()
        );
    }

    @Test
    void deveConverterEntidadeParaResponse() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Avaliacao avaliacao =
                new Avaliacao();

        avaliacao.setId(10L);
        avaliacao.setCurso(curso);
        avaliacao.setTitulo(
                "Avaliação de conhecimentos básicos"
        );
        avaliacao.setDescricao(
                "Avaliação dos conteúdos iniciais."
        );
        avaliacao.setOrdem(1);
        avaliacao.setNotaMinima(
                new BigDecimal("7.00")
        );
        avaliacao.setMaximoTentativas(3);
        avaliacao.setTempoLimiteMinutos(60);
        avaliacao.setStatus(
                StatusAvaliacao.PUBLICADA
        );
        avaliacao.prePersist();

        AvaliacaoResponse response =
                avaliacaoMapper.toResponse(
                        avaliacao
                );

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getCursoId());

        assertEquals(
                "Informática Básica",
                response.getCursoTitulo()
        );

        assertEquals(
                "Avaliação de conhecimentos básicos",
                response.getTitulo()
        );

        assertEquals(
                "Avaliação dos conteúdos iniciais.",
                response.getDescricao()
        );

        assertEquals(1, response.getOrdem());

        assertEquals(
                new BigDecimal("7.00"),
                response.getNotaMinima()
        );

        assertEquals(
                3,
                response.getMaximoTentativas()
        );

        assertEquals(
                60,
                response.getTempoLimiteMinutos()
        );

        assertEquals(
                StatusAvaliacao.PUBLICADA,
                response.getStatus()
        );

        assertNotNull(
                response.getDataCriacao()
        );

        assertNotNull(
                response.getDataAtualizacao()
        );
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
