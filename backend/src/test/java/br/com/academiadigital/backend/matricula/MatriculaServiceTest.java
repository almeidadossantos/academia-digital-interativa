package br.com.academiadigital.backend.matricula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.CursoRepository;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.matricula.dto.MatriculaRequest;
import br.com.academiadigital.backend.matricula.dto.MatriculaResponse;
import br.com.academiadigital.backend.matricula.dto.MatriculaStatusUpdateRequest;
import br.com.academiadigital.backend.matricula.mapper.MatriculaMapper;
import br.com.academiadigital.backend.usuario.Perfil;
import br.com.academiadigital.backend.usuario.Usuario;
import br.com.academiadigital.backend.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private MatriculaMapper matriculaMapper;

    @Mock
    private Pageable pageable;

    private MatriculaService matriculaService;

    @BeforeEach
    void configurar() {
        matriculaService = new MatriculaService(
                matriculaRepository,
                usuarioRepository,
                cursoRepository,
                matriculaMapper
        );
    }

    @Test
    void deveCriarMatriculaComSucesso() {
        MatriculaRequest request = criarRequest();

        Usuario aluno = criarAluno(
                1L,
                Perfil.ALUNO,
                true
        );

        Curso curso = criarCurso(2L);

        Matricula matricula =
                criarMatricula(aluno, curso);

        MatriculaResponse response =
                criarResponse(StatusMatricula.ATIVA);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(aluno));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.of(curso));

        when(matriculaRepository
                .existsByAlunoIdAndCursoId(1L, 2L))
                .thenReturn(false);

        when(matriculaMapper.toEntity(
                request,
                aluno,
                curso
        )).thenReturn(matricula);

        when(matriculaRepository.save(matricula))
                .thenReturn(matricula);

        when(matriculaMapper.toResponse(matricula))
                .thenReturn(response);

        MatriculaResponse resultado =
                matriculaService.criar(request);

        assertSame(response, resultado);

        verify(matriculaRepository).save(matricula);
        verify(matriculaMapper).toResponse(matricula);
    }

    @Test
    void deveLancarExcecaoQuandoAlunoNaoExistir() {
        MatriculaRequest request = criarRequest();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> matriculaService.criar(request)
                );

        assertEquals(
                "Aluno não encontrado com o ID: 1",
                excecao.getMessage()
        );

        verifyNoInteractions(
                cursoRepository,
                matriculaRepository,
                matriculaMapper
        );
    }

    @Test
    void deveRejeitarUsuarioQueNaoSejaAluno() {
        MatriculaRequest request = criarRequest();

        Usuario professor = criarAluno(
                1L,
                Perfil.PROFESSOR,
                true
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(professor));

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> matriculaService.criar(request)
                );

        assertEquals(
                "O usuário informado não possui o perfil ALUNO.",
                excecao.getMessage()
        );

        verifyNoInteractions(
                cursoRepository,
                matriculaRepository,
                matriculaMapper
        );
    }

    @Test
    void deveRejeitarAlunoInativo() {
        MatriculaRequest request = criarRequest();

        Usuario aluno = criarAluno(
                1L,
                Perfil.ALUNO,
                false
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(aluno));

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> matriculaService.criar(request)
                );

        assertEquals(
                "Não é possível matricular um aluno inativo.",
                excecao.getMessage()
        );

        verifyNoInteractions(
                cursoRepository,
                matriculaRepository,
                matriculaMapper
        );
    }

    @Test
    void deveLancarExcecaoQuandoCursoNaoExistir() {
        MatriculaRequest request = criarRequest();

        Usuario aluno = criarAluno(
                1L,
                Perfil.ALUNO,
                true
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(aluno));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> matriculaService.criar(request)
                );

        assertEquals(
                "Curso não encontrado com o ID: 2",
                excecao.getMessage()
        );

        verify(matriculaRepository, never())
                .save(any(Matricula.class));

        verifyNoInteractions(matriculaMapper);
    }

    @Test
    void deveRejeitarMatriculaDuplicada() {
        MatriculaRequest request = criarRequest();

        Usuario aluno = criarAluno(
                1L,
                Perfil.ALUNO,
                true
        );

        Curso curso = criarCurso(2L);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(aluno));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.of(curso));

        when(matriculaRepository
                .existsByAlunoIdAndCursoId(1L, 2L))
                .thenReturn(true);

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> matriculaService.criar(request)
                );

        assertEquals(
                "O aluno já está matriculado neste curso.",
                excecao.getMessage()
        );

        verify(matriculaRepository, never())
                .save(any(Matricula.class));

        verifyNoInteractions(matriculaMapper);
    }

    @Test
    void deveListarMatriculasComFiltros() {
        Usuario aluno = criarAluno(
                1L,
                Perfil.ALUNO,
                true
        );

        Curso curso = criarCurso(2L);

        Matricula matricula =
                criarMatricula(aluno, curso);

        MatriculaResponse response =
                criarResponse(StatusMatricula.ATIVA);

        Page<Matricula> pagina =
                new PageImpl<>(List.of(matricula));

        when(matriculaRepository.findAll(
                ArgumentMatchers
                        .<Specification<Matricula>>any(),
                same(pageable)
        )).thenReturn(pagina);

        when(matriculaMapper.toResponse(matricula))
                .thenReturn(response);

        Page<MatriculaResponse> resultado =
                matriculaService.listarTodos(
                        1L,
                        2L,
                        StatusMatricula.ATIVA,
                        pageable
                );

        assertEquals(1, resultado.getTotalElements());
        assertSame(response, resultado.getContent().get(0));

        verify(matriculaRepository).findAll(
                ArgumentMatchers
                        .<Specification<Matricula>>any(),
                same(pageable)
        );
    }

    @Test
    void deveBuscarMatriculaPorId() {
        Matricula matricula = criarMatricula(
                criarAluno(
                        1L,
                        Perfil.ALUNO,
                        true
                ),
                criarCurso(2L)
        );

        MatriculaResponse response =
                criarResponse(StatusMatricula.ATIVA);

        when(matriculaRepository.findById(10L))
                .thenReturn(Optional.of(matricula));

        when(matriculaMapper.toResponse(matricula))
                .thenReturn(response);

        MatriculaResponse resultado =
                matriculaService.buscarPorId(10L);

        assertSame(response, resultado);
    }

    @Test
    void deveLancarExcecaoAoBuscarMatriculaInexistente() {
        when(matriculaRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> matriculaService.buscarPorId(999L)
                );

        assertEquals(
                "Matrícula não encontrada com o ID: 999",
                excecao.getMessage()
        );

        verifyNoInteractions(matriculaMapper);
    }

    @Test
    void deveAtualizarStatusParaConcluida() {
        Matricula matricula =
                prepararMatriculaParaAtualizacao();

        matricula.setDataCancelamento(
                LocalDateTime.now().minusDays(1)
        );

        MatriculaStatusUpdateRequest request =
                criarStatusRequest(
                        StatusMatricula.CONCLUIDA
                );

        MatriculaResponse response =
                criarResponse(StatusMatricula.CONCLUIDA);

        configurarAtualizacaoStatus(
                matricula,
                response
        );

        MatriculaResponse resultado =
                matriculaService.atualizarStatus(
                        10L,
                        request
                );

        assertSame(response, resultado);

        assertEquals(
                StatusMatricula.CONCLUIDA,
                matricula.getStatus()
        );

        assertNotNull(matricula.getDataConclusao());
        assertNull(matricula.getDataCancelamento());

        verifyNoInteractions(
                usuarioRepository,
                cursoRepository
        );
    }

    @Test
    void deveAtualizarStatusParaCancelada() {
        Matricula matricula =
                prepararMatriculaParaAtualizacao();

        matricula.setDataConclusao(
                LocalDateTime.now().minusDays(1)
        );

        MatriculaStatusUpdateRequest request =
                criarStatusRequest(
                        StatusMatricula.CANCELADA
                );

        MatriculaResponse response =
                criarResponse(StatusMatricula.CANCELADA);

        configurarAtualizacaoStatus(
                matricula,
                response
        );

        MatriculaResponse resultado =
                matriculaService.atualizarStatus(
                        10L,
                        request
                );

        assertSame(response, resultado);

        assertEquals(
                StatusMatricula.CANCELADA,
                matricula.getStatus()
        );

        assertNotNull(matricula.getDataCancelamento());
        assertNull(matricula.getDataConclusao());
    }

    @Test
    void deveRejeitarReativacaoDeMatriculaCancelada() {
        Matricula matricula =
                prepararMatriculaParaAtualizacao();

        LocalDateTime dataCancelamento =
                LocalDateTime.now().minusDays(1);

        matricula.setStatus(
                StatusMatricula.CANCELADA
        );

        matricula.setDataCancelamento(
                dataCancelamento
        );

        MatriculaStatusUpdateRequest request =
                criarStatusRequest(
                        StatusMatricula.ATIVA
                );

        when(matriculaRepository.findById(10L))
                .thenReturn(Optional.of(matricula));

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> matriculaService.atualizarStatus(
                                10L,
                                request
                        )
                );

        assertEquals(
                "Não é possível alterar uma matrícula "
                        + "CANCELADA para ATIVA.",
                excecao.getMessage()
        );

        assertEquals(
                StatusMatricula.CANCELADA,
                matricula.getStatus()
        );

        assertEquals(
                dataCancelamento,
                matricula.getDataCancelamento()
        );

        verify(matriculaRepository, never())
                .save(any(Matricula.class));

        verifyNoInteractions(matriculaMapper);
    }

    @Test
    void deveManterStatusQuandoReceberMesmoStatus() {
        Matricula matricula =
                prepararMatriculaParaAtualizacao();

        LocalDateTime dataCancelamento =
                LocalDateTime.now().minusDays(1);

        matricula.setStatus(
                StatusMatricula.CANCELADA
        );

        matricula.setDataCancelamento(
                dataCancelamento
        );

        MatriculaStatusUpdateRequest request =
                criarStatusRequest(
                        StatusMatricula.CANCELADA
                );

        MatriculaResponse response =
                criarResponse(
                        StatusMatricula.CANCELADA
                );

        configurarAtualizacaoStatus(
                matricula,
                response
        );

        MatriculaResponse resultado =
                matriculaService.atualizarStatus(
                        10L,
                        request
                );

        assertSame(response, resultado);

        assertEquals(
                StatusMatricula.CANCELADA,
                matricula.getStatus()
        );

        assertEquals(
                dataCancelamento,
                matricula.getDataCancelamento()
        );

        assertNull(matricula.getDataConclusao());

        verify(matriculaRepository)
                .save(matricula);
    }

    @Test
    void deveLancarExcecaoAoAtualizarStatusDeMatriculaInexistente() {
        MatriculaStatusUpdateRequest request =
                criarStatusRequest(
                        StatusMatricula.CONCLUIDA
                );

        when(matriculaRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> matriculaService.atualizarStatus(
                                999L,
                                request
                        )
                );

        assertEquals(
                "Matrícula não encontrada com o ID: 999",
                excecao.getMessage()
        );

        verifyNoInteractions(
                usuarioRepository,
                cursoRepository,
                matriculaMapper
        );

        verify(matriculaRepository, never())
                .save(any(Matricula.class));
    }

    private void configurarAtualizacaoStatus(
            Matricula matricula,
            MatriculaResponse response) {

        when(matriculaRepository.findById(10L))
                .thenReturn(Optional.of(matricula));

        when(matriculaRepository.save(matricula))
                .thenReturn(matricula);

        when(matriculaMapper.toResponse(matricula))
                .thenReturn(response);
    }

    private Matricula prepararMatriculaParaAtualizacao() {
        Usuario aluno = criarAluno(
                1L,
                Perfil.ALUNO,
                true
        );

        Curso curso = criarCurso(2L);

        return criarMatricula(aluno, curso);
    }

    private MatriculaRequest criarRequest() {
        MatriculaRequest request =
                new MatriculaRequest();

        request.setAlunoId(1L);
        request.setCursoId(2L);

        return request;
    }

    private MatriculaStatusUpdateRequest criarStatusRequest(
            StatusMatricula status) {

        MatriculaStatusUpdateRequest request =
                new MatriculaStatusUpdateRequest();

        request.setStatus(status);

        return request;
    }

    private Usuario criarAluno(
            Long id,
            Perfil perfil,
            boolean ativo) {

        Usuario aluno = new Usuario();

        aluno.setId(id);
        aluno.setNome("João Silva");
        aluno.setEmail("joao@email.com");
        aluno.setPerfil(perfil);
        aluno.setAtivo(ativo);

        return aluno;
    }

    private Curso criarCurso(Long id) {
        Curso curso = new Curso();

        curso.setId(id);
        curso.setTitulo("Informática Básica");

        return curso;
    }

    private Matricula criarMatricula(
            Usuario aluno,
            Curso curso) {

        Matricula matricula = new Matricula();

        matricula.setId(10L);
        matricula.setAluno(aluno);
        matricula.setCurso(curso);
        matricula.setStatus(StatusMatricula.ATIVA);
        matricula.setDataMatricula(LocalDateTime.now());
        matricula.setDataAtualizacao(LocalDateTime.now());

        return matricula;
    }

    private MatriculaResponse criarResponse(
            StatusMatricula status) {

        MatriculaResponse response =
                new MatriculaResponse();

        response.setId(10L);
        response.setAlunoId(1L);
        response.setAlunoNome("João Silva");
        response.setAlunoEmail("joao@email.com");
        response.setCursoId(2L);
        response.setCursoTitulo("Informática Básica");
        response.setStatus(status);

        return response;
    }
}