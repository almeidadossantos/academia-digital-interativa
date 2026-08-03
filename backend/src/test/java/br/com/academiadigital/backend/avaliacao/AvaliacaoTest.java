package br.com.academiadigital.backend.avaliacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.curso.Curso;

class AvaliacaoTest {

    @Test
    void deveAtribuirERetornarOsDadosDaAvaliacao() {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setTitulo("Informática Básica");

        Avaliacao avaliacao = new Avaliacao();

        avaliacao.setId(10L);
        avaliacao.setCurso(curso);
        avaliacao.setTitulo(
                "Avaliação de conhecimentos básicos"
        );
        avaliacao.setDescricao(
                "Avaliação sobre os conteúdos iniciais do curso."
        );
        avaliacao.setOrdem(1);
        avaliacao.setNotaMinima(
                new BigDecimal("7.00")
        );
        avaliacao.setMaximoTentativas(3);
        avaliacao.setTempoLimiteMinutos(60);
        avaliacao.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        assertEquals(10L, avaliacao.getId());
        assertSame(curso, avaliacao.getCurso());
        assertEquals(
                "Avaliação de conhecimentos básicos",
                avaliacao.getTitulo()
        );
        assertEquals(
                "Avaliação sobre os conteúdos iniciais do curso.",
                avaliacao.getDescricao()
        );
        assertEquals(1, avaliacao.getOrdem());
        assertEquals(
                new BigDecimal("7.00"),
                avaliacao.getNotaMinima()
        );
        assertEquals(
                3,
                avaliacao.getMaximoTentativas()
        );
        assertEquals(
                60,
                avaliacao.getTempoLimiteMinutos()
        );
        assertEquals(
                StatusAvaliacao.PUBLICADA,
                avaliacao.getStatus()
        );
    }

    @Test
    void deveDefinirValoresPadraoAntesDePersistir() {
        Avaliacao avaliacao = new Avaliacao();

        avaliacao.prePersist();

        assertEquals(
                StatusAvaliacao.RASCUNHO,
                avaliacao.getStatus()
        );
        assertNotNull(
                avaliacao.getDataCriacao()
        );
        assertNotNull(
                avaliacao.getDataAtualizacao()
        );
    }

    @Test
    void devePreservarStatusEDataCriacaoEAtualizarDataDeModificacao()
            throws InterruptedException {

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        avaliacao.prePersist();

        StatusAvaliacao statusOriginal =
                avaliacao.getStatus();

        LocalDateTime dataCriacaoOriginal =
                avaliacao.getDataCriacao();

        LocalDateTime dataAtualizacaoOriginal =
                avaliacao.getDataAtualizacao();

        Thread.sleep(5);

        avaliacao.prePersist();

        assertSame(
                statusOriginal,
                avaliacao.getStatus()
        );
        assertEquals(
                dataCriacaoOriginal,
                avaliacao.getDataCriacao()
        );
        assertTrue(
                avaliacao.getDataAtualizacao()
                        .isAfter(dataAtualizacaoOriginal)
        );

        LocalDateTime antesDoPreUpdate =
                avaliacao.getDataAtualizacao();

        Thread.sleep(5);

        avaliacao.preUpdate();

        assertTrue(
                avaliacao.getDataAtualizacao()
                        .isAfter(antesDoPreUpdate)
        );
    }
}
