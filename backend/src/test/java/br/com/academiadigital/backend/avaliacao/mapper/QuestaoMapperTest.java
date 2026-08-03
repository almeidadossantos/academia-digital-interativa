package br.com.academiadigital.backend.avaliacao.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.avaliacao.Avaliacao;
import br.com.academiadigital.backend.avaliacao.Questao;
import br.com.academiadigital.backend.avaliacao.TipoQuestao;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoUpdateRequest;

class QuestaoMapperTest {

    private final QuestaoMapper questaoMapper =
            new QuestaoMapper();

    @Test
    void deveConverterRequestParaEntidade() {
        Avaliacao avaliacao = criarAvaliacao(
                1L,
                "Avaliação inicial"
        );

        QuestaoRequest request =
                new QuestaoRequest();

        request.setAvaliacaoId(1L);
        request.setEnunciado(
                "  Qual componente executa as instruções?  "
        );
        request.setTipo(
                TipoQuestao.MULTIPLA_ESCOLHA
        );
        request.setOrdem(1);
        request.setPontuacao(
                new BigDecimal("2.50")
        );

        Questao questao =
                questaoMapper.toEntity(
                        request,
                        avaliacao
                );

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
    void deveAtualizarEntidadeComDadosDoRequest() {
        Avaliacao avaliacaoOriginal =
                criarAvaliacao(
                        1L,
                        "Avaliação original"
                );

        Avaliacao novaAvaliacao =
                criarAvaliacao(
                        2L,
                        "Nova avaliação"
                );

        Questao questao = new Questao();

        questao.setAvaliacao(
                avaliacaoOriginal
        );
        questao.setEnunciado(
                "Enunciado original"
        );
        questao.setTipo(
                TipoQuestao.VERDADEIRO_FALSO
        );
        questao.setOrdem(1);
        questao.setPontuacao(
                new BigDecimal("1.00")
        );

        QuestaoUpdateRequest request =
                new QuestaoUpdateRequest();

        request.setAvaliacaoId(2L);
        request.setEnunciado(
                "  Enunciado atualizado  "
        );
        request.setTipo(
                TipoQuestao.DISSERTATIVA
        );
        request.setOrdem(3);
        request.setPontuacao(
                new BigDecimal("4.00")
        );

        questaoMapper.updateEntity(
                questao,
                request,
                novaAvaliacao
        );

        assertSame(
                novaAvaliacao,
                questao.getAvaliacao()
        );

        assertEquals(
                "Enunciado atualizado",
                questao.getEnunciado()
        );

        assertEquals(
                TipoQuestao.DISSERTATIVA,
                questao.getTipo()
        );

        assertEquals(3, questao.getOrdem());

        assertEquals(
                new BigDecimal("4.00"),
                questao.getPontuacao()
        );
    }

    @Test
    void deveConverterEntidadeParaResponse() {
        Avaliacao avaliacao = criarAvaliacao(
                1L,
                "Avaliação inicial"
        );

        LocalDateTime dataCriacao =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        20,
                        0
                );

        LocalDateTime dataAtualizacao =
                LocalDateTime.of(
                        2026,
                        8,
                        2,
                        21,
                        0
                );

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
        questao.setDataCriacao(
                dataCriacao
        );
        questao.setDataAtualizacao(
                dataAtualizacao
        );

        QuestaoResponse response =
                questaoMapper.toResponse(
                        questao
                );

        assertEquals(10L, response.getId());
        assertEquals(
                1L,
                response.getAvaliacaoId()
        );

        assertEquals(
                "Avaliação inicial",
                response.getAvaliacaoTitulo()
        );

        assertEquals(
                "Qual componente executa as instruções?",
                response.getEnunciado()
        );

        assertEquals(
                TipoQuestao.MULTIPLA_ESCOLHA,
                response.getTipo()
        );

        assertEquals(1, response.getOrdem());

        assertEquals(
                new BigDecimal("2.50"),
                response.getPontuacao()
        );

        assertNotNull(
                response.getDataCriacao()
        );

        assertNotNull(
                response.getDataAtualizacao()
        );

        assertEquals(
                dataCriacao,
                response.getDataCriacao()
        );

        assertEquals(
                dataAtualizacao,
                response.getDataAtualizacao()
        );
    }

    private Avaliacao criarAvaliacao(
            Long id,
            String titulo) {

        Avaliacao avaliacao =
                new Avaliacao();

        avaliacao.setId(id);
        avaliacao.setTitulo(titulo);

        return avaliacao;
    }
}
