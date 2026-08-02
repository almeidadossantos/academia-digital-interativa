package br.com.academiadigital.backend.progresso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.academiadigital.backend.aula.Aula;
import br.com.academiadigital.backend.aula.AulaRepository;
import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.matricula.Matricula;
import br.com.academiadigital.backend.matricula.MatriculaRepository;
import br.com.academiadigital.backend.progresso.dto.ProgressoAulaResponse;
import br.com.academiadigital.backend.progresso.dto.ProgressoCursoResponse;
import br.com.academiadigital.backend.progresso.mapper.ProgressoAulaMapper;
import br.com.academiadigital.backend.usuario.Perfil;
import br.com.academiadigital.backend.usuario.Usuario;
import br.com.academiadigital.backend.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ProgressoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private ProgressoAulaRepository progressoAulaRepository;

    private ProgressoService progressoService;

    @BeforeEach
    void configurar() {
        progressoService =
                new ProgressoService(
                        usuarioRepository,
                        matriculaRepository,
                        aulaRepository,
                        progressoAulaRepository,
                        new ProgressoAulaMapper()
                );
    }

    @Test
    void deveRetornarProgressoDoCurso() {
        Usuario aluno =
                criarUsuario(
                        1L,
                        Perfil.ALUNO
                );

        Curso curso =
                criarCurso(
                        10L,
                        "Informática Básica"
                );

        Matricula matricula =
                criarMatricula(
                        20L,
                        aluno,
                        curso
                );

        Aula aulaUm =
                criarAula(
                        30L,
                        curso,
                        "Introdução",
                        1
                );

        Aula aulaDois =
                criarAula(
                        31L,
                        curso,
                        "Componentes",
                        2
                );

        ProgressoAula progressoConcluido =
                criarProgresso(
                        40L,
                        matricula,
                        aulaUm,
                        true
                );

        when(
                usuarioRepository
                        .findByEmailIgnoreCase(
                                "aluno@email.com"
                        )
        ).thenReturn(
                Optional.of(aluno)
        );

        when(
                matriculaRepository
                        .findByAlunoIdAndCursoId(
                                1L,
                                10L
                        )
        ).thenReturn(
                Optional.of(matricula)
        );

        when(
                aulaRepository
                        .findAllByCursoIdOrderByOrdemAsc(
                                10L
                        )
        ).thenReturn(
                List.of(
                        aulaUm,
                        aulaDois
                )
        );

        when(
                progressoAulaRepository
                        .findAllByMatriculaIdOrderByAulaOrdemAsc(
                                20L
                        )
        ).thenReturn(
                List.of(progressoConcluido)
        );

        ProgressoCursoResponse response =
                progressoService
                        .buscarProgressoCurso(
                                "aluno@email.com",
                                10L
                        );

        assertEquals(
                20L,
                response.getMatriculaId()
        );

        assertEquals(
                10L,
                response.getCursoId()
        );

        assertEquals(
                "Informática Básica",
                response.getCursoTitulo()
        );

        assertEquals(
                2,
                response.getTotalAulas()
        );

        assertEquals(
                1,
                response.getAulasConcluidas()
        );

        assertEquals(
                50.0,
                response.getPercentualConclusao()
        );

        assertEquals(
                2,
                response.getAulas().size()
        );

        assertEquals(
                30L,
                response.getAulas()
                        .get(0)
                        .getAulaId()
        );

        assertTrue(
                response.getAulas()
                        .get(0)
                        .getConcluida()
        );

        assertEquals(
                31L,
                response.getAulas()
                        .get(1)
                        .getAulaId()
        );

        assertFalse(
                response.getAulas()
                        .get(1)
                        .getConcluida()
        );
    }

    @Test
    void deveRetornarPercentualZeroQuandoCursoNaoPossuirAulas() {
        Usuario aluno =
                criarUsuario(
                        1L,
                        Perfil.ALUNO
                );

        Curso curso =
                criarCurso(
                        10L,
                        "Curso sem aulas"
                );

        Matricula matricula =
                criarMatricula(
                        20L,
                        aluno,
                        curso
                );

        when(
                usuarioRepository
                        .findByEmailIgnoreCase(
                                "aluno@email.com"
                        )
        ).thenReturn(
                Optional.of(aluno)
        );

        when(
                matriculaRepository
                        .findByAlunoIdAndCursoId(
                                1L,
                                10L
                        )
        ).thenReturn(
                Optional.of(matricula)
        );

        when(
                aulaRepository
                        .findAllByCursoIdOrderByOrdemAsc(
                                10L
                        )
        ).thenReturn(
                List.of()
        );

        when(
                progressoAulaRepository
                        .findAllByMatriculaIdOrderByAulaOrdemAsc(
                                20L
                        )
        ).thenReturn(
                List.of()
        );

        ProgressoCursoResponse response =
                progressoService
                        .buscarProgressoCurso(
                                "aluno@email.com",
                                10L
                        );

        assertEquals(
                0,
                response.getTotalAulas()
        );

        assertEquals(
                0,
                response.getAulasConcluidas()
        );

        assertEquals(
                0.0,
                response.getPercentualConclusao()
        );

        assertTrue(
                response.getAulas().isEmpty()
        );
    }

    @Test
    void deveRejeitarUsuarioSemPerfilAluno() {
        Usuario administrador =
                criarUsuario(
                        1L,
                        Perfil.ADMIN
                );

        when(
                usuarioRepository
                        .findByEmailIgnoreCase(
                                "admin@email.com"
                        )
        ).thenReturn(
                Optional.of(administrador)
        );

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                progressoService
                                        .buscarProgressoCurso(
                                                "admin@email.com",
                                                10L
                                        )
                );

        assertEquals(
                "O usuário autenticado não possui o perfil ALUNO.",
                excecao.getMessage()
        );
    }

    @Test
    void deveFalharQuandoAlunoAutenticadoNaoExistir() {
        when(
                usuarioRepository
                        .findByEmailIgnoreCase(
                                "inexistente@email.com"
                        )
        ).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                progressoService
                                        .buscarProgressoCurso(
                                                "inexistente@email.com",
                                                10L
                                        )
                );

        assertEquals(
                "Aluno autenticado não encontrado.",
                excecao.getMessage()
        );
    }

    @Test
    void deveFalharQuandoAlunoNaoEstiverMatriculadoNoCurso() {
        Usuario aluno =
                criarUsuario(
                        1L,
                        Perfil.ALUNO
                );

        when(
                usuarioRepository
                        .findByEmailIgnoreCase(
                                "aluno@email.com"
                        )
        ).thenReturn(
                Optional.of(aluno)
        );

        when(
                matriculaRepository
                        .findByAlunoIdAndCursoId(
                                1L,
                                10L
                        )
        ).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                progressoService
                                        .buscarProgressoCurso(
                                                "aluno@email.com",
                                                10L
                                        )
                );

        assertEquals(
                "Matrícula não encontrada para o aluno autenticado "
                        + "no curso de ID: 10",
                excecao.getMessage()
        );
    }

    @Test
    void deveConcluirAulaCriandoProgressoQuandoNaoExistir() {
        Usuario aluno =
                criarUsuario(
                        1L,
                        Perfil.ALUNO
                );

        Curso curso =
                criarCurso(
                        10L,
                        "Informática Básica"
                );

        Matricula matricula =
                criarMatricula(
                        20L,
                        aluno,
                        curso
                );

        Aula aula =
                criarAula(
                        30L,
                        curso,
                        "Introdução",
                        1
                );

        when(
                usuarioRepository
                        .findByEmailIgnoreCase(
                                "aluno@email.com"
                        )
        ).thenReturn(
                Optional.of(aluno)
        );

        when(
                aulaRepository.findById(30L)
        ).thenReturn(
                Optional.of(aula)
        );

        when(
                matriculaRepository
                        .findByAlunoIdAndCursoId(
                                1L,
                                10L
                        )
        ).thenReturn(
                Optional.of(matricula)
        );

        when(
                progressoAulaRepository
                        .findByMatriculaIdAndAulaId(
                                20L,
                                30L
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                progressoAulaRepository.save(
                        any(ProgressoAula.class)
                )
        ).thenAnswer(invocacao -> {
            ProgressoAula progresso =
                    invocacao.getArgument(0);

            progresso.setId(40L);

            return progresso;
        });

        ProgressoAulaResponse response =
                progressoService.concluirAula(
                        "aluno@email.com",
                        30L
                );

        ArgumentCaptor<ProgressoAula> captor =
                ArgumentCaptor.forClass(
                        ProgressoAula.class
                );

        verify(progressoAulaRepository)
                .save(captor.capture());

        ProgressoAula progressoSalvo =
                captor.getValue();

        assertSame(
                matricula,
                progressoSalvo.getMatricula()
        );

        assertSame(
                aula,
                progressoSalvo.getAula()
        );

        assertTrue(
                progressoSalvo.getConcluida()
        );

        assertEquals(
                40L,
                response.getId()
        );

        assertEquals(
                20L,
                response.getMatriculaId()
        );

        assertEquals(
                30L,
                response.getAulaId()
        );

        assertTrue(
                response.getConcluida()
        );
    }

    @Test
    void deveConcluirAulaAtualizandoProgressoExistente() {
        Usuario aluno =
                criarUsuario(
                        1L,
                        Perfil.ALUNO
                );

        Curso curso =
                criarCurso(
                        10L,
                        "Informática Básica"
                );

        Matricula matricula =
                criarMatricula(
                        20L,
                        aluno,
                        curso
                );

        Aula aula =
                criarAula(
                        30L,
                        curso,
                        "Introdução",
                        1
                );

        ProgressoAula progressoExistente =
                criarProgresso(
                        40L,
                        matricula,
                        aula,
                        false
                );

        when(
                usuarioRepository
                        .findByEmailIgnoreCase(
                                "aluno@email.com"
                        )
        ).thenReturn(
                Optional.of(aluno)
        );

        when(
                aulaRepository.findById(30L)
        ).thenReturn(
                Optional.of(aula)
        );

        when(
                matriculaRepository
                        .findByAlunoIdAndCursoId(
                                1L,
                                10L
                        )
        ).thenReturn(
                Optional.of(matricula)
        );

        when(
                progressoAulaRepository
                        .findByMatriculaIdAndAulaId(
                                20L,
                                30L
                        )
        ).thenReturn(
                Optional.of(progressoExistente)
        );

        when(
                progressoAulaRepository.save(
                        progressoExistente
                )
        ).thenReturn(
                progressoExistente
        );

        ProgressoAulaResponse response =
                progressoService.concluirAula(
                        "aluno@email.com",
                        30L
                );

        assertTrue(
                progressoExistente.getConcluida()
        );

        assertEquals(
                40L,
                response.getId()
        );

        assertTrue(
                response.getConcluida()
        );

        verify(progressoAulaRepository)
                .save(progressoExistente);
    }

    @Test
    void deveFalharAoConcluirAulaInexistente() {
        Usuario aluno =
                criarUsuario(
                        1L,
                        Perfil.ALUNO
                );

        when(
                usuarioRepository
                        .findByEmailIgnoreCase(
                                "aluno@email.com"
                        )
        ).thenReturn(
                Optional.of(aluno)
        );

        when(
                aulaRepository.findById(999L)
        ).thenReturn(
                Optional.empty()
        );

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                progressoService.concluirAula(
                                        "aluno@email.com",
                                        999L
                                )
                );

        assertEquals(
                "Aula não encontrada com ID: 999",
                excecao.getMessage()
        );
    }

    private Usuario criarUsuario(
            Long id,
            Perfil perfil) {

        Usuario usuario = new Usuario();

        usuario.setId(id);
        usuario.setPerfil(perfil);

        return usuario;
    }

    private Curso criarCurso(
            Long id,
            String titulo) {

        Curso curso = new Curso();

        curso.setId(id);
        curso.setTitulo(titulo);

        return curso;
    }

    private Matricula criarMatricula(
            Long id,
            Usuario aluno,
            Curso curso) {

        Matricula matricula =
                new Matricula();

        matricula.setId(id);
        matricula.setAluno(aluno);
        matricula.setCurso(curso);

        return matricula;
    }

    private Aula criarAula(
            Long id,
            Curso curso,
            String titulo,
            Integer ordem) {

        Aula aula = new Aula();

        aula.setId(id);
        aula.setCurso(curso);
        aula.setTitulo(titulo);
        aula.setOrdem(ordem);

        return aula;
    }

    private ProgressoAula criarProgresso(
            Long id,
            Matricula matricula,
            Aula aula,
            Boolean concluida) {

        ProgressoAula progresso =
                new ProgressoAula();

        progresso.setId(id);
        progresso.setMatricula(matricula);
        progresso.setAula(aula);
        progresso.setConcluida(concluida);

        return progresso;
    }
}
