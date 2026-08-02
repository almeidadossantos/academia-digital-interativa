package br.com.academiadigital.backend.progresso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.aula.Aula;
import br.com.academiadigital.backend.aula.AulaRepository;
import br.com.academiadigital.backend.aula.StatusAula;
import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.CursoRepository;
import br.com.academiadigital.backend.curso.NivelCurso;
import br.com.academiadigital.backend.curso.StatusCurso;
import br.com.academiadigital.backend.matricula.Matricula;
import br.com.academiadigital.backend.matricula.MatriculaRepository;
import br.com.academiadigital.backend.matricula.StatusMatricula;
import br.com.academiadigital.backend.usuario.Perfil;
import br.com.academiadigital.backend.usuario.Usuario;
import br.com.academiadigital.backend.usuario.UsuarioRepository;

@SpringBootTest
@Transactional
class ProgressoAulaRepositoryTest {

    @Autowired
    private ProgressoAulaRepository
            progressoAulaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CursoRepository cursoRepository;

    @Autowired
    private AulaRepository aulaRepository;

    @Autowired
    private MatriculaRepository matriculaRepository;

    private Matricula matricula;
    private Aula primeiraAula;
    private Aula segundaAula;

    @BeforeEach
    void configurarDados() {
        Usuario aluno =
                usuarioRepository.saveAndFlush(
                        criarAluno()
                );

        Curso curso =
                cursoRepository.saveAndFlush(
                        criarCurso()
                );

        primeiraAula =
                aulaRepository.saveAndFlush(
                        criarAula(
                                curso,
                                "Introdução ao curso",
                                1
                        )
                );

        segundaAula =
                aulaRepository.saveAndFlush(
                        criarAula(
                                curso,
                                "Conteúdo prático",
                                2
                        )
                );

        matricula =
                matriculaRepository.saveAndFlush(
                        criarMatricula(
                                aluno,
                                curso
                        )
                );
    }

    @Test
    void deveEncontrarProgressoPorMatriculaEAula() {
        ProgressoAula progressoSalvo =
                salvarProgresso(
                        primeiraAula,
                        true
                );

        Optional<ProgressoAula> resultado =
                progressoAulaRepository
                        .findByMatriculaIdAndAulaId(
                                matricula.getId(),
                                primeiraAula.getId()
                        );

        assertTrue(resultado.isPresent());

        assertEquals(
                progressoSalvo.getId(),
                resultado.get().getId()
        );

        assertEquals(
                primeiraAula.getId(),
                resultado.get()
                        .getAula()
                        .getId()
        );
    }

    @Test
    void deveListarProgressosOrdenadosPelaOrdemDaAula() {
        salvarProgresso(
                segundaAula,
                false
        );

        salvarProgresso(
                primeiraAula,
                true
        );

        List<ProgressoAula> resultado =
                progressoAulaRepository
                        .findAllByMatriculaIdOrderByAulaOrdemAsc(
                                matricula.getId()
                        );

        assertEquals(
                2,
                resultado.size()
        );

        assertEquals(
                1,
                resultado.get(0)
                        .getAula()
                        .getOrdem()
        );

        assertEquals(
                primeiraAula.getId(),
                resultado.get(0)
                        .getAula()
                        .getId()
        );

        assertEquals(
                2,
                resultado.get(1)
                        .getAula()
                        .getOrdem()
        );

        assertEquals(
                segundaAula.getId(),
                resultado.get(1)
                        .getAula()
                        .getId()
        );
    }

    @Test
    void deveContarSomenteAulasConcluidas() {
        salvarProgresso(
                primeiraAula,
                true
        );

        salvarProgresso(
                segundaAula,
                false
        );

        long quantidadeConcluida =
                progressoAulaRepository
                        .countByMatriculaIdAndConcluidaTrue(
                                matricula.getId()
                        );

        assertEquals(
                1L,
                quantidadeConcluida
        );
    }

    private ProgressoAula salvarProgresso(
            Aula aula,
            boolean concluida) {

        ProgressoAula progresso =
                new ProgressoAula();

        progresso.setMatricula(matricula);
        progresso.setAula(aula);
        progresso.setConcluida(concluida);

        return progressoAulaRepository
                .saveAndFlush(progresso);
    }

    private Usuario criarAluno() {
        Usuario aluno = new Usuario();

        aluno.setNome(
                "Aluno de integração"
        );

        aluno.setEmail(
                "aluno.progresso."
                        + UUID.randomUUID()
                        + "@email.com"
        );

        aluno.setSenha(
                "senha-criptografada-teste"
        );

        aluno.setPerfil(
                Perfil.ALUNO
        );

        aluno.setAtivo(true);

        return aluno;
    }

    private Curso criarCurso() {
        Curso curso = new Curso();

        curso.setTitulo(
                "Curso de progresso "
                        + UUID.randomUUID()
        );

        curso.setDescricao(
                "Curso utilizado nos testes de integração "
                        + "do progresso das aulas."
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

    private Aula criarAula(
            Curso curso,
            String titulo,
            Integer ordem) {

        Aula aula = new Aula();

        aula.setCurso(curso);
        aula.setTitulo(titulo);

        aula.setDescricao(
                "Aula utilizada nos testes "
                        + "de progresso."
        );

        aula.setOrdem(ordem);
        aula.setDuracaoMinutos(30);

        aula.setStatus(
                StatusAula.PUBLICADA
        );

        return aula;
    }

    private Matricula criarMatricula(
            Usuario aluno,
            Curso curso) {

        Matricula novaMatricula =
                new Matricula();

        novaMatricula.setAluno(aluno);
        novaMatricula.setCurso(curso);

        novaMatricula.setStatus(
                StatusMatricula.ATIVA
        );

        return novaMatricula;
    }
}