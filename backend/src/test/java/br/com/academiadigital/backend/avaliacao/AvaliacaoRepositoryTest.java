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
class AvaliacaoRepositoryTest {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private CursoRepository cursoRepository;

    private Curso curso;

    @BeforeEach
    void configurarDados() {
        curso =
                cursoRepository.saveAndFlush(
                        criarCurso()
                );
    }

    @Test
    void deveVerificarExistenciaPorCursoEOrdem() {
        salvarAvaliacao(
                "Avaliação inicial",
                1
        );

        boolean ordemExistente =
                avaliacaoRepository
                        .existsByCursoIdAndOrdem(
                                curso.getId(),
                                1
                        );

        boolean ordemInexistente =
                avaliacaoRepository
                        .existsByCursoIdAndOrdem(
                                curso.getId(),
                                2
                        );

        assertTrue(ordemExistente);
        assertFalse(ordemInexistente);
    }

    @Test
    void deveDesconsiderarPropriaAvaliacaoAoVerificarOrdem() {
        Avaliacao primeiraAvaliacao =
                salvarAvaliacao(
                        "Primeira avaliação",
                        1
                );

        Avaliacao segundaAvaliacao =
                salvarAvaliacao(
                        "Segunda avaliação",
                        2
                );

        boolean conflitoComPropriaAvaliacao =
                avaliacaoRepository
                        .existsByCursoIdAndOrdemAndIdNot(
                                curso.getId(),
                                1,
                                primeiraAvaliacao.getId()
                        );

        boolean conflitoComOutraAvaliacao =
                avaliacaoRepository
                        .existsByCursoIdAndOrdemAndIdNot(
                                curso.getId(),
                                1,
                                segundaAvaliacao.getId()
                        );

        assertFalse(
                conflitoComPropriaAvaliacao
        );

        assertTrue(
                conflitoComOutraAvaliacao
        );
    }

    @Test
    void deveListarAvaliacoesDoCursoOrdenadasPorOrdem() {
        Avaliacao segundaAvaliacao =
                salvarAvaliacao(
                        "Segunda avaliação",
                        2
                );

        Avaliacao primeiraAvaliacao =
                salvarAvaliacao(
                        "Primeira avaliação",
                        1
                );

        List<Avaliacao> resultado =
                avaliacaoRepository
                        .findAllByCursoIdOrderByOrdemAsc(
                                curso.getId()
                        );

        assertEquals(
                2,
                resultado.size()
        );

        assertEquals(
                primeiraAvaliacao.getId(),
                resultado.get(0).getId()
        );

        assertEquals(
                1,
                resultado.get(0).getOrdem()
        );

        assertEquals(
                segundaAvaliacao.getId(),
                resultado.get(1).getId()
        );

        assertEquals(
                2,
                resultado.get(1).getOrdem()
        );
    }

    private Avaliacao salvarAvaliacao(
            String titulo,
            Integer ordem) {

        Avaliacao avaliacao =
                new Avaliacao();

        avaliacao.setCurso(curso);
        avaliacao.setTitulo(titulo);

        avaliacao.setDescricao(
                "Avaliação utilizada nos testes "
                        + "de integração do repositório."
        );

        avaliacao.setOrdem(ordem);

        avaliacao.setNotaMinima(
                new BigDecimal("7.00")
        );

        avaliacao.setMaximoTentativas(3);
        avaliacao.setTempoLimiteMinutos(60);

        avaliacao.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        return avaliacaoRepository
                .saveAndFlush(avaliacao);
    }

    private Curso criarCurso() {
        Curso novoCurso =
                new Curso();

        novoCurso.setTitulo(
                "Curso de avaliações "
                        + UUID.randomUUID()
        );

        novoCurso.setDescricao(
                "Curso utilizado nos testes "
                        + "de integração das avaliações."
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
