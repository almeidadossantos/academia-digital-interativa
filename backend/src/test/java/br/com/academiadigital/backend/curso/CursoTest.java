package br.com.academiadigital.backend.curso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class CursoTest {

    @Test
    void deveAtribuirERetornarOsDadosDoCurso() {
        Curso curso = new Curso();

        curso.setId(1L);
        curso.setTitulo("Java para iniciantes");
        curso.setDescricao("Curso introdutório de Java.");
        curso.setCargaHoraria(40);
        curso.setNivel(NivelCurso.INICIANTE);
        curso.setStatus(StatusCurso.PUBLICADO);
        curso.setImagemUrl("https://exemplo.com/java.png");

        assertEquals(1L, curso.getId());
        assertEquals("Java para iniciantes", curso.getTitulo());
        assertEquals("Curso introdutório de Java.", curso.getDescricao());
        assertEquals(40, curso.getCargaHoraria());
        assertEquals(NivelCurso.INICIANTE, curso.getNivel());
        assertEquals(StatusCurso.PUBLICADO, curso.getStatus());
        assertEquals("https://exemplo.com/java.png", curso.getImagemUrl());
    }

    @Test
    void deveDefinirValoresPadraoAntesDePersistir() {
        Curso curso = new Curso();

        curso.prePersist();

        assertEquals(StatusCurso.RASCUNHO, curso.getStatus());
        assertNotNull(curso.getDataCriacao());
        assertNotNull(curso.getDataAtualizacao());
    }

    @Test
    void devePreservarStatusEDataCriacaoEAtualizarDataDeModificacao()
            throws InterruptedException {

        Curso curso = new Curso();
        curso.setStatus(StatusCurso.PUBLICADO);

        curso.prePersist();

        StatusCurso statusOriginal = curso.getStatus();
        LocalDateTime dataCriacaoOriginal = curso.getDataCriacao();
        LocalDateTime dataAtualizacaoOriginal = curso.getDataAtualizacao();

        Thread.sleep(5);

        curso.prePersist();

        assertSame(statusOriginal, curso.getStatus());
        assertEquals(dataCriacaoOriginal, curso.getDataCriacao());
        assertTrue(
                curso.getDataAtualizacao()
                        .isAfter(dataAtualizacaoOriginal)
        );

        LocalDateTime antesDoPreUpdate = curso.getDataAtualizacao();

        Thread.sleep(5);

        curso.preUpdate();

        assertTrue(
                curso.getDataAtualizacao()
                        .isAfter(antesDoPreUpdate)
        );
    }
}