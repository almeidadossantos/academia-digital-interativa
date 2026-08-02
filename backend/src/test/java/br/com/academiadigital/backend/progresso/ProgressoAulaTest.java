package br.com.academiadigital.backend.progresso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.aula.Aula;
import br.com.academiadigital.backend.matricula.Matricula;

class ProgressoAulaTest {

    @Test
    void deveInicializarComoNaoConcluido() {
        ProgressoAula progresso =
                new ProgressoAula();

        progresso.prePersist();

        assertFalse(progresso.getConcluida());
        assertNull(progresso.getDataConclusao());
        assertNotNull(progresso.getDataCriacao());
        assertNotNull(progresso.getDataAtualizacao());
    }

    @Test
    void deveRegistrarDataAoConcluirAula() {
        ProgressoAula progresso =
                new ProgressoAula();

        progresso.setConcluida(true);
        progresso.prePersist();

        assertTrue(progresso.getConcluida());
        assertNotNull(progresso.getDataConclusao());
        assertNotNull(progresso.getDataCriacao());
        assertNotNull(progresso.getDataAtualizacao());
    }

    @Test
    void deveRemoverDataConclusaoAoReabrirAula() {
        ProgressoAula progresso =
                new ProgressoAula();

        progresso.setConcluida(true);
        progresso.prePersist();

        assertNotNull(progresso.getDataConclusao());

        progresso.setConcluida(false);
        progresso.preUpdate();

        assertFalse(progresso.getConcluida());
        assertNull(progresso.getDataConclusao());
        assertNotNull(progresso.getDataAtualizacao());
    }

    @Test
    void deveArmazenarIdentificadorEMapeamentos() {
        ProgressoAula progresso =
                new ProgressoAula();

        Matricula matricula =
                new Matricula();

        Aula aula =
                new Aula();

        progresso.setId(10L);
        progresso.setMatricula(matricula);
        progresso.setAula(aula);

        assertEquals(10L, progresso.getId());
        assertSame(
                matricula,
                progresso.getMatricula()
        );
        assertSame(
                aula,
                progresso.getAula()
        );
    }
}