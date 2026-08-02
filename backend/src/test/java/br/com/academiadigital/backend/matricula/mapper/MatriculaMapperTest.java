package br.com.academiadigital.backend.matricula.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.matricula.Matricula;
import br.com.academiadigital.backend.matricula.StatusMatricula;
import br.com.academiadigital.backend.matricula.dto.MatriculaRequest;
import br.com.academiadigital.backend.matricula.dto.MatriculaResponse;
import br.com.academiadigital.backend.usuario.Usuario;

class MatriculaMapperTest {

    private MatriculaMapper matriculaMapper;

    @BeforeEach
    void configurar() {
        matriculaMapper = new MatriculaMapper();
    }

    @Test
    void deveConverterRequestParaEntidadeAtiva() {
        MatriculaRequest request =
                new MatriculaRequest();

        request.setAlunoId(1L);
        request.setCursoId(2L);

        Usuario aluno = criarAluno();
        Curso curso = criarCurso();

        Matricula matricula =
                matriculaMapper.toEntity(
                        request,
                        aluno,
                        curso
                );

        assertSame(aluno, matricula.getAluno());
        assertSame(curso, matricula.getCurso());

        assertEquals(
                StatusMatricula.ATIVA,
                matricula.getStatus()
        );
    }

    @Test
    void deveConverterEntidadeParaResponse() {
        LocalDateTime dataMatricula =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        10,
                        0
                );

        LocalDateTime dataConclusao =
                LocalDateTime.of(
                        2026,
                        8,
                        10,
                        15,
                        0
                );

        LocalDateTime dataAtualizacao =
                LocalDateTime.of(
                        2026,
                        8,
                        10,
                        15,
                        5
                );

        Usuario aluno = criarAluno();
        Curso curso = criarCurso();

        Matricula matricula = new Matricula();

        matricula.setId(10L);
        matricula.setAluno(aluno);
        matricula.setCurso(curso);
        matricula.setStatus(
                StatusMatricula.CONCLUIDA
        );
        matricula.setDataMatricula(dataMatricula);
        matricula.setDataConclusao(dataConclusao);
        matricula.setDataAtualizacao(dataAtualizacao);

        MatriculaResponse response =
                matriculaMapper.toResponse(matricula);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getAlunoId());
        assertEquals("João Silva", response.getAlunoNome());
        assertEquals(
                "joao@email.com",
                response.getAlunoEmail()
        );

        assertEquals(2L, response.getCursoId());
        assertEquals(
                "Informática Básica",
                response.getCursoTitulo()
        );

        assertEquals(
                StatusMatricula.CONCLUIDA,
                response.getStatus()
        );

        assertEquals(
                dataMatricula,
                response.getDataMatricula()
        );

        assertEquals(
                dataConclusao,
                response.getDataConclusao()
        );

        assertNull(response.getDataCancelamento());

        assertEquals(
                dataAtualizacao,
                response.getDataAtualizacao()
        );
    }

    private Usuario criarAluno() {
        Usuario aluno = new Usuario();

        aluno.setId(1L);
        aluno.setNome("João Silva");
        aluno.setEmail("joao@email.com");

        return aluno;
    }

    private Curso criarCurso() {
        Curso curso = new Curso();

        curso.setId(2L);
        curso.setTitulo("Informática Básica");

        return curso;
    }
}