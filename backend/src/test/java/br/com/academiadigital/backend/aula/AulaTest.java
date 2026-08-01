package br.com.academiadigital.backend.aula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.curso.Curso;

class AulaTest {

    @Test
    void deveAtribuirERetornarOsDadosDaAula() {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setTitulo("Informática Básica");

        Aula aula = new Aula();

        aula.setId(10L);
        aula.setCurso(curso);
        aula.setTitulo("Introdução ao computador");
        aula.setDescricao(
                "Apresentação dos componentes básicos do computador."
        );
        aula.setOrdem(1);
        aula.setDuracaoMinutos(30);
        aula.setVideoUrl(
                "https://exemplo.com/videos/introducao"
        );
        aula.setStatus(StatusAula.PUBLICADA);

        assertEquals(10L, aula.getId());
        assertSame(curso, aula.getCurso());
        assertEquals(
                "Introdução ao computador",
                aula.getTitulo()
        );
        assertEquals(
                "Apresentação dos componentes básicos do computador.",
                aula.getDescricao()
        );
        assertEquals(1, aula.getOrdem());
        assertEquals(30, aula.getDuracaoMinutos());
        assertEquals(
                "https://exemplo.com/videos/introducao",
                aula.getVideoUrl()
        );
        assertEquals(
                StatusAula.PUBLICADA,
                aula.getStatus()
        );
    }

    @Test
    void deveDefinirValoresPadraoAntesDePersistir() {
        Aula aula = new Aula();

        aula.prePersist();

        assertEquals(
                StatusAula.RASCUNHO,
                aula.getStatus()
        );
        assertNotNull(aula.getDataCriacao());
        assertNotNull(aula.getDataAtualizacao());
    }

    @Test
    void devePreservarStatusEDataCriacaoEAtualizarDataDeModificacao()
            throws InterruptedException {

        Aula aula = new Aula();
        aula.setStatus(StatusAula.PUBLICADA);

        aula.prePersist();

        StatusAula statusOriginal = aula.getStatus();
        LocalDateTime dataCriacaoOriginal =
                aula.getDataCriacao();
        LocalDateTime dataAtualizacaoOriginal =
                aula.getDataAtualizacao();

        Thread.sleep(5);

        aula.prePersist();

        assertSame(statusOriginal, aula.getStatus());
        assertEquals(
                dataCriacaoOriginal,
                aula.getDataCriacao()
        );
        assertTrue(
                aula.getDataAtualizacao()
                        .isAfter(dataAtualizacaoOriginal)
        );

        LocalDateTime antesDoPreUpdate =
                aula.getDataAtualizacao();

        Thread.sleep(5);

        aula.preUpdate();

        assertTrue(
                aula.getDataAtualizacao()
                        .isAfter(antesDoPreUpdate)
        );
    }
}