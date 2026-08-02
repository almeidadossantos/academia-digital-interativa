package br.com.academiadigital.backend.trilha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class TrilhaTest {

    @Test
    void deveInicializarStatusEDatasAntesDePersistir() {
        Trilha trilha = new Trilha();

        assertNull(trilha.getStatus());
        assertNull(trilha.getDataCriacao());
        assertNull(trilha.getDataAtualizacao());

        trilha.prePersist();

        assertEquals(
                StatusTrilha.RASCUNHO,
                trilha.getStatus()
        );

        assertNotNull(
                trilha.getDataCriacao()
        );

        assertNotNull(
                trilha.getDataAtualizacao()
        );
    }

    @Test
    void devePreservarStatusEDataCriacaoAoExecutarPrePersistNovamente()
            throws InterruptedException {

        Trilha trilha = new Trilha();

        trilha.setStatus(
                StatusTrilha.PUBLICADA
        );

        trilha.prePersist();

        StatusTrilha statusOriginal =
                trilha.getStatus();

        LocalDateTime dataCriacaoOriginal =
                trilha.getDataCriacao();

        LocalDateTime dataAtualizacaoOriginal =
                trilha.getDataAtualizacao();

        Thread.sleep(5);

        trilha.prePersist();

        assertSame(
                statusOriginal,
                trilha.getStatus()
        );

        assertEquals(
                dataCriacaoOriginal,
                trilha.getDataCriacao()
        );

        assertTrue(
                trilha.getDataAtualizacao()
                        .isAfter(
                                dataAtualizacaoOriginal
                        )
        );
    }

    @Test
    void deveAtualizarDataAntesDeAtualizarEntidade()
            throws InterruptedException {

        Trilha trilha = new Trilha();

        trilha.prePersist();

        LocalDateTime dataAtualizacaoOriginal =
                trilha.getDataAtualizacao();

        Thread.sleep(5);

        trilha.preUpdate();

        assertTrue(
                trilha.getDataAtualizacao()
                        .isAfter(
                                dataAtualizacaoOriginal
                        )
        );
    }
}