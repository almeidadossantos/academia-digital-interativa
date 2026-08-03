package br.com.academiadigital.backend.tentativa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class RespostaQuestaoTest {

    @Test
    void devePreencherValoresPadraoAoPersistir() {
        RespostaQuestao resposta = new RespostaQuestao();

        resposta.prePersist();

        assertFalse(resposta.getCorrigida());
        assertEquals(
                BigDecimal.ZERO,
                resposta.getPontuacaoObtida()
        );
        assertNotNull(resposta.getDataCriacao());
        assertNotNull(resposta.getDataAtualizacao());
    }

    @Test
    void devePreservarValoresInformadosAoPersistir() {
        RespostaQuestao resposta = new RespostaQuestao();
        LocalDateTime dataCriacao =
                LocalDateTime.of(2026, 8, 3, 10, 0);

        resposta.setCorrigida(true);
        resposta.setPontuacaoObtida(
                new BigDecimal("2.50")
        );
        resposta.setDataCriacao(dataCriacao);
        resposta.prePersist();

        assertEquals(true, resposta.getCorrigida());
        assertEquals(
                new BigDecimal("2.50"),
                resposta.getPontuacaoObtida()
        );
        assertEquals(dataCriacao, resposta.getDataCriacao());
    }

    @Test
    void deveAtualizarDataDeAtualizacao() {
        RespostaQuestao resposta = new RespostaQuestao();

        resposta.setDataAtualizacao(
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
        resposta.preUpdate();

        assertNotNull(resposta.getDataAtualizacao());
    }
}
