package br.com.academiadigital.backend.curso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.academiadigital.backend.curso.dto.CursoRequest;
import br.com.academiadigital.backend.curso.dto.CursoResponse;
import br.com.academiadigital.backend.curso.dto.CursoUpdateRequest;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class CursoServiceTest {

    @Mock
    private CursoRepository cursoRepository;

    private CursoService cursoService;

    @BeforeEach
    void configurar() {
        cursoService = new CursoService(cursoRepository);
    }

    @Test
    void deveCriarCursoComStatusPadrao() {
        CursoRequest request = criarRequest();
        request.setTitulo("  Java para iniciantes  ");
        request.setStatus(null);

        when(cursoRepository.existsByTituloIgnoreCase(
                "Java para iniciantes"
        )).thenReturn(false);

        when(cursoRepository.save(any(Curso.class)))
                .thenAnswer(invocation -> {
                    Curso curso = invocation.getArgument(0);
                    curso.setId(1L);
                    return curso;
                });

        CursoResponse response = cursoService.criar(request);

        assertEquals(1L, response.getId());
        assertEquals("Java para iniciantes", response.getTitulo());
        assertEquals(StatusCurso.RASCUNHO, response.getStatus());

        ArgumentCaptor<Curso> captor =
                ArgumentCaptor.forClass(Curso.class);

        verify(cursoRepository).save(captor.capture());
        assertEquals(
                "Java para iniciantes",
                captor.getValue().getTitulo()
        );
        assertEquals(
                StatusCurso.RASCUNHO,
                captor.getValue().getStatus()
        );
    }

    @Test
    void deveCriarCursoComStatusInformado() {
        CursoRequest request = criarRequest();
        request.setStatus(StatusCurso.PUBLICADO);

        when(cursoRepository.existsByTituloIgnoreCase(
                "Curso de Java"
        )).thenReturn(false);

        when(cursoRepository.save(any(Curso.class)))
                .thenAnswer(invocation -> {
                    Curso curso = invocation.getArgument(0);
                    curso.setId(2L);
                    return curso;
                });

        CursoResponse response = cursoService.criar(request);

        assertEquals(StatusCurso.PUBLICADO, response.getStatus());
    }

    @Test
    void deveLancarExcecaoAoCriarCursoComTituloDuplicado() {
        CursoRequest request = criarRequest();

        when(cursoRepository.existsByTituloIgnoreCase(
                "Curso de Java"
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cursoService.criar(request)
        );

        assertEquals(
                "Já existe um curso com este título.",
                exception.getMessage()
        );

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void deveListarCursosComFiltrosEPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);

        Curso curso = criarCursoExistente();
        Page<Curso> pagina = new PageImpl<>(
                List.of(curso),
                pageable,
                1
        );

        when(cursoRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(pagina);

        Page<CursoResponse> resultado = cursoService.listarTodos(
                "java",
                NivelCurso.INICIANTE,
                StatusCurso.PUBLICADO,
                pageable
        );

        assertEquals(1, resultado.getTotalElements());
        assertEquals(
                "Curso existente",
                resultado.getContent().get(0).getTitulo()
        );

        verify(cursoRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );
    }

    @Test
    void deveBuscarCursoPorId() {
        Curso curso = criarCursoExistente();

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        CursoResponse response = cursoService.buscarPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("Curso existente", response.getTitulo());
    }

    @Test
    void deveLancarExcecaoAoBuscarCursoInexistente() {
        when(cursoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cursoService.buscarPorId(99L)
        );

        assertEquals(
                "Curso não encontrado com o ID: 99",
                exception.getMessage()
        );
    }

    @Test
    void deveAtualizarCursoComTituloAlterado() {
        Curso curso = criarCursoExistente();
        CursoUpdateRequest request = criarUpdateRequest();
        request.setTitulo("  Spring Boot avançado  ");
        request.setStatus(StatusCurso.INATIVO);

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        when(cursoRepository.existsByTituloIgnoreCase(
                "Spring Boot avançado"
        )).thenReturn(false);

        when(cursoRepository.save(curso))
                .thenReturn(curso);

        CursoResponse response =
                cursoService.atualizar(1L, request);

        assertEquals(
                "Spring Boot avançado",
                response.getTitulo()
        );
        assertEquals(NivelCurso.AVANCADO, response.getNivel());
        assertEquals(StatusCurso.INATIVO, response.getStatus());

        verify(cursoRepository).existsByTituloIgnoreCase(
                "Spring Boot avançado"
        );
        verify(cursoRepository).save(curso);
    }

    @Test
    void deveAtualizarSemConsultarDuplicidadeQuandoTituloNaoMuda() {
        Curso curso = criarCursoExistente();
        CursoUpdateRequest request = criarUpdateRequest();
        request.setTitulo("  CURSO EXISTENTE  ");
        request.setStatus(null);

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        when(cursoRepository.save(curso))
                .thenReturn(curso);

        CursoResponse response =
                cursoService.atualizar(1L, request);

        assertEquals("CURSO EXISTENTE", response.getTitulo());
        assertEquals(StatusCurso.PUBLICADO, response.getStatus());

        verify(
                cursoRepository,
                never()
        ).existsByTituloIgnoreCase(any(String.class));

        verify(cursoRepository).save(curso);
    }

    @Test
    void deveLancarExcecaoAoAtualizarParaTituloDuplicado() {
        Curso curso = criarCursoExistente();
        CursoUpdateRequest request = criarUpdateRequest();
        request.setTitulo("Outro curso");

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        when(cursoRepository.existsByTituloIgnoreCase(
                "Outro curso"
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cursoService.atualizar(1L, request)
        );

        assertEquals(
                "Já existe um curso com este título.",
                exception.getMessage()
        );

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarCursoInexistente() {
        CursoUpdateRequest request = criarUpdateRequest();

        when(cursoRepository.findById(50L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cursoService.atualizar(50L, request)
        );

        verify(
                cursoRepository,
                never()
        ).existsByTituloIgnoreCase(any(String.class));

        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    void deveExcluirCursoExistente() {
        Curso curso = criarCursoExistente();

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        cursoService.excluir(1L);

        verify(cursoRepository).delete(curso);
    }

    @Test
    void deveLancarExcecaoAoExcluirCursoInexistente() {
        when(cursoRepository.findById(80L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> cursoService.excluir(80L)
        );

        verify(cursoRepository, never()).delete(any(Curso.class));
    }

    private CursoRequest criarRequest() {
        CursoRequest request = new CursoRequest();
        request.setTitulo("Curso de Java");
        request.setDescricao(
                "Curso introdutório de desenvolvimento em Java."
        );
        request.setCargaHoraria(40);
        request.setNivel(NivelCurso.INICIANTE);
        request.setImagemUrl(
                "https://exemplo.com/curso-java.png"
        );
        return request;
    }

    private CursoUpdateRequest criarUpdateRequest() {
        CursoUpdateRequest request = new CursoUpdateRequest();
        request.setTitulo("Spring Boot avançado");
        request.setDescricao("Conteúdo atualizado.");
        request.setCargaHoraria(80);
        request.setNivel(NivelCurso.AVANCADO);
        request.setImagemUrl(
                "https://exemplo.com/spring-avancado.png"
        );
        return request;
    }

    private Curso criarCursoExistente() {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setTitulo("Curso existente");
        curso.setDescricao("Descrição existente.");
        curso.setCargaHoraria(40);
        curso.setNivel(NivelCurso.INICIANTE);
        curso.setStatus(StatusCurso.PUBLICADO);
        curso.setImagemUrl(
                "https://exemplo.com/curso.png"
        );
        return curso;
    }
}
