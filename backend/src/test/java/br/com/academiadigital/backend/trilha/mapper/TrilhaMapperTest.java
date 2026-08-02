package br.com.academiadigital.backend.trilha.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.trilha.StatusTrilha;
import br.com.academiadigital.backend.trilha.Trilha;
import br.com.academiadigital.backend.trilha.TrilhaCurso;
import br.com.academiadigital.backend.trilha.dto.TrilhaRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaResponse;

class TrilhaMapperTest {

    private TrilhaMapper trilhaMapper;

    @BeforeEach
    void configurar() {
        TrilhaCursoMapper trilhaCursoMapper =
                new TrilhaCursoMapper();

        trilhaMapper =
                new TrilhaMapper(
                        trilhaCursoMapper
                );
    }

    @Test
    void deveConverterRequestParaEntityComStatusPadrao() {
        TrilhaRequest request =
                criarRequest();

        request.setStatus(null);

        Trilha resultado =
                trilhaMapper.toEntity(request);

        assertEquals(
                "Formação em Java",
                resultado.getTitulo()
        );

        assertEquals(
                "Trilha completa para aprendizagem de Java.",
                resultado.getDescricao()
        );

        assertEquals(
                StatusTrilha.RASCUNHO,
                resultado.getStatus()
        );
    }

    @Test
    void deveConverterRequestParaEntityComStatusInformado() {
        TrilhaRequest request =
                criarRequest();

        request.setStatus(
                StatusTrilha.PUBLICADA
        );

        Trilha resultado =
                trilhaMapper.toEntity(request);

        assertEquals(
                StatusTrilha.PUBLICADA,
                resultado.getStatus()
        );
    }

    @Test
    void deveAtualizarEntityPreservandoStatusQuandoNaoInformado() {
        Trilha trilha = new Trilha();

        trilha.setTitulo("Título anterior");
        trilha.setDescricao("Descrição anterior");
        trilha.setStatus(
                StatusTrilha.PUBLICADA
        );

        TrilhaRequest request =
                criarRequest();

        request.setStatus(null);

        trilhaMapper.atualizarEntity(
                trilha,
                request
        );

        assertEquals(
                "Formação em Java",
                trilha.getTitulo()
        );

        assertEquals(
                "Trilha completa para aprendizagem de Java.",
                trilha.getDescricao()
        );

        assertSame(
                StatusTrilha.PUBLICADA,
                trilha.getStatus()
        );
    }

    @Test
    void deveConverterEntityECursosParaResponse() {
        LocalDateTime dataCriacao =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        20,
                        0
                );

        LocalDateTime dataAtualizacao =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        21,
                        0
                );

        Trilha trilha = new Trilha();

        trilha.setId(1L);
        trilha.setTitulo("Formação em Java");
        trilha.setDescricao(
                "Trilha completa para aprendizagem de Java."
        );
        trilha.setStatus(
                StatusTrilha.PUBLICADA
        );
        trilha.setDataCriacao(dataCriacao);
        trilha.setDataAtualizacao(
                dataAtualizacao
        );

        Curso curso = new Curso();

        curso.setId(2L);
        curso.setTitulo(
                "Java para iniciantes"
        );

        TrilhaCurso trilhaCurso =
                new TrilhaCurso();

        trilhaCurso.setId(3L);
        trilhaCurso.setTrilha(trilha);
        trilhaCurso.setCurso(curso);
        trilhaCurso.setOrdem(1);

        TrilhaResponse resultado =
                trilhaMapper.toResponse(
                        trilha,
                        List.of(trilhaCurso)
                );

        assertEquals(
                1L,
                resultado.getId()
        );

        assertEquals(
                "Formação em Java",
                resultado.getTitulo()
        );

        assertEquals(
                "Trilha completa para aprendizagem de Java.",
                resultado.getDescricao()
        );

        assertEquals(
                StatusTrilha.PUBLICADA,
                resultado.getStatus()
        );

        assertEquals(
                dataCriacao,
                resultado.getDataCriacao()
        );

        assertEquals(
                dataAtualizacao,
                resultado.getDataAtualizacao()
        );

        assertEquals(
                1,
                resultado.getCursos().size()
        );

        assertEquals(
                2L,
                resultado.getCursos()
                        .get(0)
                        .getCursoId()
        );

        assertEquals(
                "Java para iniciantes",
                resultado.getCursos()
                        .get(0)
                        .getCursoTitulo()
        );

        assertEquals(
                1,
                resultado.getCursos()
                        .get(0)
                        .getOrdem()
        );
    }

    private TrilhaRequest criarRequest() {
        TrilhaRequest request =
                new TrilhaRequest();

        request.setTitulo(
                "Formação em Java"
        );

        request.setDescricao(
                "Trilha completa para aprendizagem de Java."
        );

        return request;
    }
}