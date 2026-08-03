package br.com.academiadigital.backend.avaliacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.CursoRepository;
import br.com.academiadigital.backend.curso.NivelCurso;
import br.com.academiadigital.backend.curso.StatusCurso;

@SpringBootTest
@Transactional
class QuestaoRepositoryTest {

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    private Avaliacao avaliacao;

    @BeforeEach
    void configurarDados() {
        Curso curso =
                cursoRepository.saveAndFlush(
                        criarCurso()
                );

        avaliacao =
                avaliacaoRepository.saveAndFlush(
                        criarAvaliacao(curso)
                );
    }

    @Test
    void deveVerificarExistenciaPorAvaliacaoEOrdem() {
        salvarQuestao(
                "Qual componente executa as instruções?",
                1
        );

        boolean ordemExistente =
                questaoRepository
                        .existsByAvaliacaoIdAndOrdem(
                                avaliacao.getId(),
                                1
                        );

        boolean ordemInexistente =
                questaoRepository
                        .existsByAvaliacaoIdAndOrdem(
                                avaliacao.getId(),
                                2
                        );

        assertTrue(ordemExistente);
        assertFalse(ordemInexistente);
    }

    @Test
    void deveDesconsiderarPropriaQuestaoAoVerificarOrdem() {
        Questao primeiraQuestao =
                salvarQuestao(
                        "Primeira questão",
                        1
                );

        Questao segundaQuestao =
                salvarQuestao(
                        "Segunda questão",
                        2
                );

        boolean conflitoComPropriaQuestao =
                questaoRepository
                        .existsByAvaliacaoIdAndOrdemAndIdNot(
                                avaliacao.getId(),
                                1,
                                primeiraQuestao.getId()
                        );

        boolean conflitoComOutraQuestao =
                questaoRepository
                        .existsByAvaliacaoIdAndOrdemAndIdNot(
                                avaliacao.getId(),
                                1,
                                segundaQuestao.getId()
                        );

        assertFalse(
                conflitoComPropriaQuestao
        );

        assertTrue(
                conflitoComOutraQuestao
        );
    }

    @Test
    void deveListarQuestoesDaAvaliacaoOrdenadasPorOrdem() {
        Questao segundaQuestao =
                salvarQuestao(
                        "Segunda questão",
                        2
                );

        Questao primeiraQuestao =
                salvarQuestao(
                        "Primeira questão",
                        1
                );

        List<Questao> resultado =
                questaoRepository
                        .findAllByAvaliacaoIdOrderByOrdemAsc(
                                avaliacao.getId()
                        );

        assertEquals(
                2,
                resultado.size()
        );

        assertEquals(
                primeiraQuestao.getId(),
                resultado.get(0).getId()
        );

        assertEquals(
                1,
                resultado.get(0).getOrdem()
        );

        assertEquals(
                segundaQuestao.getId(),
                resultado.get(1).getId()
        );

        assertEquals(
                2,
                resultado.get(1).getOrdem()
        );
    }

    private Questao salvarQuestao(
            String enunciado,
            Integer ordem) {

        Questao questao =
                new Questao();

        questao.setAvaliacao(avaliacao);
        questao.setEnunciado(enunciado);

        questao.setTipo(
                TipoQuestao.MULTIPLA_ESCOLHA
        );

        questao.setOrdem(ordem);

        questao.setPontuacao(
                new BigDecimal("2.50")
        );

        return questaoRepository
                .saveAndFlush(questao);
    }

    private Avaliacao criarAvaliacao(
            Curso curso) {

        Avaliacao novaAvaliacao =
                new Avaliacao();

        novaAvaliacao.setCurso(curso);

        novaAvaliacao.setTitulo(
                "Avaliação de questões "
                        + UUID.randomUUID()
        );

        novaAvaliacao.setDescricao(
                "Avaliação utilizada nos testes "
                        + "de integração das questões."
        );

        novaAvaliacao.setOrdem(1);

        novaAvaliacao.setNotaMinima(
                new BigDecimal("7.00")
        );

        novaAvaliacao.setMaximoTentativas(3);
        novaAvaliacao.setTempoLimiteMinutos(60);

        novaAvaliacao.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        return novaAvaliacao;
    }

    private Curso criarCurso() {
        Curso novoCurso =
                new Curso();

        novoCurso.setTitulo(
                "Curso de questões "
                        + UUID.randomUUID()
        );

        novoCurso.setDescricao(
                "Curso utilizado nos testes "
                        + "de integração das questões."
        );

        novoCurso.setCargaHoraria(40);

        novoCurso.setNivel(
                NivelCurso.INICIANTE
        );

        novoCurso.setStatus(
                StatusCurso.PUBLICADO
        );

        return novoCurso;
    }
}
