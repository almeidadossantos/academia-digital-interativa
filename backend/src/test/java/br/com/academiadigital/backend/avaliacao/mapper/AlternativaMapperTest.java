package br.com.academiadigital.backend.avaliacao.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.com.academiadigital.backend.avaliacao.Alternativa;
import br.com.academiadigital.backend.avaliacao.Questao;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaRequest;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaResponse;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaUpdateRequest;

class AlternativaMapperTest {

    private final AlternativaMapper alternativaMapper =
            new AlternativaMapper();

    @Test
    void deveConverterRequestParaEntidade() {
        Questao questao =
                criarQuestao(
                        1L,
                        "Qual componente executa as instruções?"
                );

        AlternativaRequest request =
                new AlternativaRequest();

        request.setQuestaoId(1L);

        request.setTexto(
                "  Unidade Central de Processamento  "
        );

        request.setCorreta(true);
        request.setOrdem(1);

        Alternativa alternativa =
                alternativaMapper.toEntity(
                        request,
                        questao
                );

        assertSame(
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
    void deveAtualizarEntidadeComDadosDoRequest() {
        Questao questaoOriginal =
                criarQuestao(
                        1L,
                        "Questão original"
                );

        Questao novaQuestao =
                criarQuestao(
                        2L,
                        "Nova questão"
                );

        Alternativa alternativa =
                new Alternativa();

        alternativa.setQuestao(
                questaoOriginal
        );

        alternativa.setTexto(
                "Texto original"
        );

        alternativa.setCorreta(false);
        alternativa.setOrdem(1);

        AlternativaUpdateRequest request =
                new AlternativaUpdateRequest();

        request.setQuestaoId(2L);

        request.setTexto(
                "  Texto atualizado  "
        );

        request.setCorreta(true);
        request.setOrdem(3);

        alternativaMapper.updateEntity(
                alternativa,
                request,
                novaQuestao
        );

        assertSame(
                novaQuestao,
                alternativa.getQuestao()
        );

        assertEquals(
                "Texto atualizado",
                alternativa.getTexto()
        );

        assertEquals(
                true,
                alternativa.getCorreta()
        );

        assertEquals(
                3,
                alternativa.getOrdem()
        );
    }

    @Test
    void deveConverterEntidadeParaResponse() {
        Questao questao =
                criarQuestao(
                        1L,
                        "Qual componente executa as instruções?"
                );

        LocalDateTime dataCriacao =
                LocalDateTime.of(
                        2026,
                        8,
                        3,
                        1,
                        0
                );

        LocalDateTime dataAtualizacao =
                LocalDateTime.of(
                        2026,
                        8,
                        3,
                        1,
                        30
                );

        Alternativa alternativa =
                new Alternativa();

        alternativa.setId(10L);
        alternativa.setQuestao(questao);

        alternativa.setTexto(
                "Unidade Central de Processamento"
        );

        alternativa.setCorreta(true);
        alternativa.setOrdem(1);

        alternativa.setDataCriacao(
                dataCriacao
        );

        alternativa.setDataAtualizacao(
                dataAtualizacao
        );

        AlternativaResponse response =
                alternativaMapper.toResponse(
                        alternativa
                );

        assertEquals(
                10L,
                response.getId()
        );

        assertEquals(
                1L,
                response.getQuestaoId()
        );

        assertEquals(
                "Qual componente executa as instruções?",
                response.getQuestaoEnunciado()
        );

        assertEquals(
                "Unidade Central de Processamento",
                response.getTexto()
        );

        assertEquals(
                true,
                response.getCorreta()
        );

        assertEquals(
                1,
                response.getOrdem()
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

    private Questao criarQuestao(
            Long id,
            String enunciado) {

        Questao questao =
                new Questao();

        questao.setId(id);
        questao.setEnunciado(enunciado);

        return questao;
    }
}
