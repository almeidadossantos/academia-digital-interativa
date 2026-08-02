package br.com.academiadigital.backend.trilha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
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
class TrilhaCursoRepositoryTest {

    @Autowired
    private TrilhaCursoRepository
            trilhaCursoRepository;

    @Autowired
    private TrilhaRepository trilhaRepository;

    @Autowired
    private CursoRepository cursoRepository;

    private Trilha trilha;
    private Curso primeiroCurso;
    private Curso segundoCurso;

    @BeforeEach
    void configurarDados() {
        trilha = trilhaRepository.saveAndFlush(
                criarTrilha()
        );

        primeiroCurso =
                cursoRepository.saveAndFlush(
                        criarCurso(
                                "Java básico "
                                        + UUID.randomUUID()
                        )
                );

        segundoCurso =
                cursoRepository.saveAndFlush(
                        criarCurso(
                                "Spring Boot "
                                        + UUID.randomUUID()
                        )
                );
    }

    @Test
    void deveListarCursosOrdenadosPelaOrdem() {
        salvarAssociacao(
                segundoCurso,
                2
        );

        salvarAssociacao(
                primeiroCurso,
                1
        );

        List<TrilhaCurso> resultado =
                trilhaCursoRepository
                        .findAllByTrilhaIdOrderByOrdemAsc(
                                trilha.getId()
                        );

        assertEquals(
                2,
                resultado.size()
        );

        assertEquals(
                1,
                resultado.get(0).getOrdem()
        );

        assertEquals(
                primeiroCurso.getId(),
                resultado.get(0)
                        .getCurso()
                        .getId()
        );

        assertEquals(
                2,
                resultado.get(1).getOrdem()
        );
    }

    @Test
    void deveEncontrarAssociacaoPorTrilhaECurso() {
        TrilhaCurso associacao =
                salvarAssociacao(
                        primeiroCurso,
                        1
                );

        Optional<TrilhaCurso> resultado =
                trilhaCursoRepository
                        .findByTrilhaIdAndCursoId(
                                trilha.getId(),
                                primeiroCurso.getId()
                        );

        assertTrue(resultado.isPresent());

        assertEquals(
                associacao.getId(),
                resultado.get().getId()
        );
    }

    @Test
    void deveDetectarCursoJaAssociado() {
        salvarAssociacao(
                primeiroCurso,
                1
        );

        boolean cursoAssociado =
                trilhaCursoRepository
                        .existsByTrilhaIdAndCursoId(
                                trilha.getId(),
                                primeiroCurso.getId()
                        );

        boolean cursoNaoAssociado =
                trilhaCursoRepository
                        .existsByTrilhaIdAndCursoId(
                                trilha.getId(),
                                segundoCurso.getId()
                        );

        assertTrue(cursoAssociado);
        assertFalse(cursoNaoAssociado);
    }

    @Test
    void deveDetectarOrdemJaUtilizada() {
        salvarAssociacao(
                primeiroCurso,
                1
        );

        boolean ordemUtilizada =
                trilhaCursoRepository
                        .existsByTrilhaIdAndOrdem(
                                trilha.getId(),
                                1
                        );

        boolean ordemDisponivel =
                trilhaCursoRepository
                        .existsByTrilhaIdAndOrdem(
                                trilha.getId(),
                                2
                        );

        assertTrue(ordemUtilizada);
        assertFalse(ordemDisponivel);
    }

    @Test
    void deveDesconsiderarAssociacaoAtualAoValidarOrdem() {
        TrilhaCurso primeiraAssociacao =
                salvarAssociacao(
                        primeiroCurso,
                        1
                );

        salvarAssociacao(
                segundoCurso,
                2
        );

        boolean mesmaAssociacao =
                trilhaCursoRepository
                        .existsByTrilhaIdAndOrdemAndIdNot(
                                trilha.getId(),
                                1,
                                primeiraAssociacao.getId()
                        );

        boolean outraAssociacao =
                trilhaCursoRepository
                        .existsByTrilhaIdAndOrdemAndIdNot(
                                trilha.getId(),
                                2,
                                primeiraAssociacao.getId()
                        );

        assertFalse(mesmaAssociacao);
        assertTrue(outraAssociacao);
    }

    @Test
    void deveExcluirTodasAsAssociacoesDaTrilha() {
        salvarAssociacao(
                primeiroCurso,
                1
        );

        salvarAssociacao(
                segundoCurso,
                2
        );

        trilhaCursoRepository
                .deleteAllByTrilhaId(
                        trilha.getId()
                );

        trilhaCursoRepository.flush();

        List<TrilhaCurso> resultado =
                trilhaCursoRepository
                        .findAllByTrilhaIdOrderByOrdemAsc(
                                trilha.getId()
                        );

        assertTrue(resultado.isEmpty());
    }

    private TrilhaCurso salvarAssociacao(
            Curso curso,
            Integer ordem) {

        TrilhaCurso associacao =
                new TrilhaCurso();

        associacao.setTrilha(trilha);
        associacao.setCurso(curso);
        associacao.setOrdem(ordem);

        return trilhaCursoRepository
                .saveAndFlush(associacao);
    }

    private Trilha criarTrilha() {
        Trilha novaTrilha = new Trilha();

        novaTrilha.setTitulo(
                "Trilha de integração "
                        + UUID.randomUUID()
        );

        novaTrilha.setDescricao(
                "Trilha utilizada nos testes "
                        + "do repositório de associações."
        );

        novaTrilha.setStatus(
                StatusTrilha.RASCUNHO
        );

        return novaTrilha;
    }

    private Curso criarCurso(
            String titulo) {

        Curso curso = new Curso();

        curso.setTitulo(titulo);

        curso.setDescricao(
                "Curso utilizado nos testes "
                        + "de integração das trilhas."
        );

        curso.setCargaHoraria(40);

        curso.setNivel(
                NivelCurso.INICIANTE
        );

        curso.setStatus(
                StatusCurso.PUBLICADO
        );

        return curso;
    }
}