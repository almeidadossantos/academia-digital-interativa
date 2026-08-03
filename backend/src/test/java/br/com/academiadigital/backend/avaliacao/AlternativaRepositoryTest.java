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
class AlternativaRepositoryTest {

    @Autowired
    private AlternativaRepository alternativaRepository;

    @Autowired
    private QuestaoRepository questaoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    private Questao questao;

    @BeforeEach
    void configurarDados() {
        Curso curso =
                cursoRepository.saveAndFlush(
                        criarCurso()
                );

        Avaliacao avaliacao =
                avaliacaoRepository.saveAndFlush(
                        criarAvaliacao(curso)
                );

        questao =
                questaoRepository.saveAndFlush(
                        criarQuestao(avaliacao)
                );
    }

    @Test
    void deveVerificarExistenciaPorQuestaoEOrdem() {
        salvarAlternativa(
                "Unidade Central de Processamento",
                true,
                1
        );

        boolean ordemExistente =
                alternativaRepository
                        .existsByQuestaoIdAndOrdem(
                                questao.getId(),
                                1
                        );

        boolean ordemInexistente =
                alternativaRepository
                        .existsByQuestaoIdAndOrdem(
                                questao.getId(),
                                2
                        );

        assertTrue(ordemExistente);
        assertFalse(ordemInexistente);
    }

    @Test
    void deveDesconsiderarPropriaAlternativaAoVerificarOrdem() {
        Alternativa primeiraAlternativa =
                salvarAlternativa(
                        "Processador",
                        true,
                        1
                );

        Alternativa segundaAlternativa =
                salvarAlternativa(
                        "Memória RAM",
                        false,
                        2
                );

        boolean conflitoComPropriaAlternativa =
                alternativaRepository
                        .existsByQuestaoIdAndOrdemAndIdNot(
                                questao.getId(),
                                1,
                                primeiraAlternativa.getId()
                        );

        boolean conflitoComOutraAlternativa =
                alternativaRepository
                        .existsByQuestaoIdAndOrdemAndIdNot(
                                questao.getId(),
                                1,
                                segundaAlternativa.getId()
                        );

        assertFalse(
                conflitoComPropriaAlternativa
        );

        assertTrue(
                conflitoComOutraAlternativa
        );
    }

    @Test
    void deveListarAlternativasDaQuestaoOrdenadasPorOrdem() {
        Alternativa segundaAlternativa =
                salvarAlternativa(
                        "Memória RAM",
                        false,
                        2
                );

        Alternativa primeiraAlternativa =
                salvarAlternativa(
                        "Processador",
                        true,
                        1
                );

        List<Alternativa> resultado =
                alternativaRepository
                        .findAllByQuestaoIdOrderByOrdemAsc(
                                questao.getId()
                        );

        assertEquals(
                2,
                resultado.size()
        );

        assertEquals(
                primeiraAlternativa.getId(),
                resultado.get(0).getId()
        );

        assertEquals(
                1,
                resultado.get(0).getOrdem()
        );

        assertEquals(
                segundaAlternativa.getId(),
                resultado.get(1).getId()
        );

        assertEquals(
                2,
                resultado.get(1).getOrdem()
        );
    }

    private Alternativa salvarAlternativa(
            String texto,
            Boolean correta,
            Integer ordem) {

        Alternativa alternativa =
                new Alternativa();

        alternativa.setQuestao(questao);
        alternativa.setTexto(texto);
        alternativa.setCorreta(correta);
        alternativa.setOrdem(ordem);

        return alternativaRepository
                .saveAndFlush(alternativa);
    }

    private Questao criarQuestao(
            Avaliacao avaliacao) {

        Questao novaQuestao =
                new Questao();

        novaQuestao.setAvaliacao(avaliacao);

        novaQuestao.setEnunciado(
                "Qual componente executa as instruções?"
        );

        novaQuestao.setTipo(
                TipoQuestao.MULTIPLA_ESCOLHA
        );

        novaQuestao.setOrdem(1);

        novaQuestao.setPontuacao(
                new BigDecimal("2.50")
        );

        return novaQuestao;
    }

    private Avaliacao criarAvaliacao(
            Curso curso) {

        Avaliacao novaAvaliacao =
                new Avaliacao();

        novaAvaliacao.setCurso(curso);

        novaAvaliacao.setTitulo(
                "Avaliação de alternativas "
                        + UUID.randomUUID()
        );

        novaAvaliacao.setDescricao(
                "Avaliação utilizada nos testes "
                        + "de integração das alternativas."
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
                "Curso de alternativas "
                        + UUID.randomUUID()
        );

        novoCurso.setDescricao(
                "Curso utilizado nos testes "
                        + "de integração das alternativas."
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
