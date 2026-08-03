package br.com.academiadigital.backend.avaliacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class QuestaoTest {

    @Test
    void deveAtribuirERetornarOsDadosDaQuestao() {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(1L);
        avaliacao.setTitulo("Avaliação inicial");

        Questao questao = new Questao();

        questao.setId(10L);
        questao.setAvaliacao(avaliacao);
        questao.setEnunciado(
                "Qual componente executa as instruções?"
        );
        questao.setTipo(
                TipoQuestao.MULTIPLA_ESCOLHA
        );
        questao.setOrdem(1);
        questao.setPontuacao(
                new BigDecimal("2.50")
        );

        assertEquals(10L, questao.getId());
        assertSame(
                avaliacao,
                questao.getAvaliacao()
        );
        assertEquals(
                "Qual componente executa as instruções?",
                questao.getEnunciado()
        );
        assertEquals(
                TipoQuestao.MULTIPLA_ESCOLHA,
                questao.getTipo()
        );
        assertEquals(1, questao.getOrdem());
        assertEquals(
                new BigDecimal("2.50"),
                questao.getPontuacao()
        );
    }

    @Test
    void deveDefinirDatasAntesDePersistir() {
        Questao questao = new Questao();

        questao.prePersist();

        assertNotNull(
                questao.getDataCriacao()
        );
        assertNotNull(
                questao.getDataAtualizacao()
        );
    }

    @Test
    void devePreservarDataCriacaoEAtualizarDataDeModificacao()
            throws InterruptedException {

        Questao questao = new Questao();

        questao.prePersist();

        LocalDateTime dataCriacaoOriginal =
                questao.getDataCriacao();

        LocalDateTime dataAtualizacaoOriginal =
                questao.getDataAtualizacao();

        Thread.sleep(5);

        questao.prePersist();

        assertEquals(
                dataCriacaoOriginal,
                questao.getDataCriacao()
        );
        assertTrue(
                questao.getDataAtualizacao()
                        .isAfter(dataAtualizacaoOriginal)
        );

        LocalDateTime antesDoPreUpdate =
                questao.getDataAtualizacao();

        Thread.sleep(5);

        questao.preUpdate();

        assertTrue(
                questao.getDataAtualizacao()
                        .isAfter(antesDoPreUpdate)
        );
    }
}
