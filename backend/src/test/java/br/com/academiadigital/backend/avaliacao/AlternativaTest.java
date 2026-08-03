package br.com.academiadigital.backend.avaliacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class AlternativaTest {

    @Test
    void deveManterDadosDaAlternativa() {
        Questao questao = new Questao();

        Alternativa alternativa =
                new Alternativa();

        alternativa.setId(1L);
        alternativa.setQuestao(questao);
        alternativa.setTexto(
                "Unidade Central de Processamento"
        );
        alternativa.setCorreta(true);
        alternativa.setOrdem(1);

        assertEquals(
                1L,
                alternativa.getId()
        );

        assertEquals(
                questao,
                alternativa.getQuestao()
        );

        assertEquals(
                "Unidade Central de Processamento",
                alternativa.getTexto()
        );

        assertEquals(
                true,
                alternativa.getCorreta()
        );

        assertEquals(
                1,
                alternativa.getOrdem()
        );
    }

    @Test
    void devePreencherDatasAntesDePersistir() {
        Alternativa alternativa =
                new Alternativa();

        alternativa.prePersist();

        assertNotNull(
                alternativa.getDataCriacao()
        );

        assertNotNull(
                alternativa.getDataAtualizacao()
        );

        assertEquals(
                alternativa.getDataCriacao(),
                alternativa.getDataAtualizacao()
        );
    }

    @Test
    void deveAtualizarDataAntesDeModificar() {
        Alternativa alternativa =
                new Alternativa();

        LocalDateTime dataCriacao =
                LocalDateTime.now().minusDays(2);

        LocalDateTime dataAtualizacaoAnterior =
                LocalDateTime.now().minusDays(1);

        alternativa.setDataCriacao(
                dataCriacao
        );

        alternativa.setDataAtualizacao(
                dataAtualizacaoAnterior
        );

        alternativa.preUpdate();

        assertEquals(
                dataCriacao,
                alternativa.getDataCriacao()
        );

        assertTrue(
                alternativa
                        .getDataAtualizacao()
                        .isAfter(dataAtualizacaoAnterior)
        );
    }
}
