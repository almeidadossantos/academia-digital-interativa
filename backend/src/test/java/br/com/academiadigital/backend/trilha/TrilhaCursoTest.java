package br.com.academiadigital.backend.trilha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.curso.Curso;

class TrilhaCursoTest {

    @Test
    void deveArmazenarDadosDaAssociacaoEntreTrilhaECurso() {
        Trilha trilha = new Trilha();
        trilha.setId(1L);
        trilha.setTitulo("Formação em Java");

        Curso curso = new Curso();
        curso.setId(2L);
        curso.setTitulo("Java para iniciantes");

        TrilhaCurso trilhaCurso =
                new TrilhaCurso();

        trilhaCurso.setId(3L);
        trilhaCurso.setTrilha(trilha);
        trilhaCurso.setCurso(curso);
        trilhaCurso.setOrdem(1);

        assertEquals(
                3L,
                trilhaCurso.getId()
        );

        assertSame(
                trilha,
                trilhaCurso.getTrilha()
        );

        assertSame(
                curso,
                trilhaCurso.getCurso()
        );

        assertEquals(
                1,
                trilhaCurso.getOrdem()
        );
    }
}