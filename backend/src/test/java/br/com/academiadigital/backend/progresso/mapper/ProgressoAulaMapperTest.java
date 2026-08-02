package br.com.academiadigital.backend.progresso.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.aula.Aula;
import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.matricula.Matricula;
import br.com.academiadigital.backend.progresso.ProgressoAula;
import br.com.academiadigital.backend.progresso.dto.ProgressoAulaResponse;

class ProgressoAulaMapperTest {

    private ProgressoAulaMapper progressoAulaMapper;

    @BeforeEach
    void configurar() {
        progressoAulaMapper =
                new ProgressoAulaMapper();
    }

    @Test
    void deveConverterEntidadeParaResponse() {
        LocalDateTime dataConclusao =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        13,
                        30
                );

        LocalDateTime dataCriacao =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        10,
                        0
                );

        LocalDateTime dataAtualizacao =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        13,
                        30
                );

        Curso curso = new Curso();
        curso.setId(20L);
        curso.setTitulo(
                "Informática Básica"
        );

        Matricula matricula =
                new Matricula();

        matricula.setId(30L);
        matricula.setCurso(curso);

        Aula aula = new Aula();

        aula.setId(40L);
        aula.setCurso(curso);
        aula.setTitulo(
                "Introdução ao computador"
        );
        aula.setOrdem(1);

        ProgressoAula progresso =
                new ProgressoAula();

        progresso.setId(50L);
        progresso.setMatricula(matricula);
        progresso.setAula(aula);
        progresso.setConcluida(true);
        progresso.setDataConclusao(
                dataConclusao
        );
        progresso.setDataCriacao(
                dataCriacao
        );
        progresso.setDataAtualizacao(
                dataAtualizacao
        );

        ProgressoAulaResponse response =
                progressoAulaMapper.toResponse(
                        progresso
                );

        assertEquals(
                50L,
                response.getId()
        );

        assertEquals(
                30L,
                response.getMatriculaId()
        );

        assertEquals(
                20L,
                response.getCursoId()
        );

        assertEquals(
                "Informática Básica",
                response.getCursoTitulo()
        );

        assertEquals(
                40L,
                response.getAulaId()
        );

        assertEquals(
                "Introdução ao computador",
                response.getAulaTitulo()
        );

        assertEquals(
                1,
                response.getAulaOrdem()
        );

        assertEquals(
                true,
                response.getConcluida()
        );

        assertEquals(
                dataConclusao,
                response.getDataConclusao()
        );

        assertEquals(
                dataCriacao,
                response.getDataCriacao()
        );

        assertEquals(
                dataAtualizacao,
                response.getDataAtualizacao()
        );
    }
}