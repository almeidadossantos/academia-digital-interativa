package br.com.academiadigital.backend.tentativa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class TentativaAvaliacaoTest {

    @Test
    void devePreencherValoresPadraoAoPersistir() {
        TentativaAvaliacao tentativa =
                new TentativaAvaliacao();

        tentativa.prePersist();

        assertEquals(
                StatusTentativa.EM_ANDAMENTO,
                tentativa.getStatus()
        );
        assertNotNull(tentativa.getDataInicio());
        assertNotNull(tentativa.getDataAtualizacao());
    }

    @Test
    void devePreservarStatusEDataInicioInformados() {
        TentativaAvaliacao tentativa =
                new TentativaAvaliacao();
        LocalDateTime dataInicio =
                LocalDateTime.of(2026, 8, 3, 10, 0);

        tentativa.setStatus(StatusTentativa.FINALIZADA);
        tentativa.setDataInicio(dataInicio);
        tentativa.prePersist();

        assertEquals(
                StatusTentativa.FINALIZADA,
                tentativa.getStatus()
        );
        assertEquals(dataInicio, tentativa.getDataInicio());
    }

    @Test
    void deveAtualizarDataDeAtualizacao() {
        TentativaAvaliacao tentativa =
                new TentativaAvaliacao();

        tentativa.setDataAtualizacao(
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
        tentativa.preUpdate();

        assertNotNull(tentativa.getDataAtualizacao());
    }
}
