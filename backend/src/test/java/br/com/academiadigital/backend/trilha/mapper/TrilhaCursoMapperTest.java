package br.com.academiadigital.backend.trilha.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.trilha.Trilha;
import br.com.academiadigital.backend.trilha.TrilhaCurso;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoResponse;

class TrilhaCursoMapperTest {

    private TrilhaCursoMapper trilhaCursoMapper;

    @BeforeEach
    void configurar() {
        trilhaCursoMapper =
                new TrilhaCursoMapper();
    }

    @Test
    void deveConverterRequestParaEntity() {
        TrilhaCursoRequest request =
                new TrilhaCursoRequest();

        request.setCursoId(2L);
        request.setOrdem(1);

        Trilha trilha = new Trilha();
        trilha.setId(1L);

        Curso curso = new Curso();
        curso.setId(2L);

        TrilhaCurso resultado =
                trilhaCursoMapper.toEntity(
                        request,
                        trilha,
                        curso
                );

        assertSame(
                trilha,
                resultado.getTrilha()
        );

        assertSame(
                curso,
                resultado.getCurso()
        );

        assertEquals(
                1,
                resultado.getOrdem()
        );
    }

    @Test
    void deveConverterEntityParaResponse() {
        Trilha trilha = new Trilha();
        trilha.setId(1L);

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

        TrilhaCursoResponse resultado =
                trilhaCursoMapper.toResponse(
                        trilhaCurso
                );

        assertEquals(
                3L,
                resultado.getId()
        );

        assertEquals(
                2L,
                resultado.getCursoId()
        );

        assertEquals(
                "Java para iniciantes",
                resultado.getCursoTitulo()
        );

        assertEquals(
                1,
                resultado.getOrdem()
        );
    }
}