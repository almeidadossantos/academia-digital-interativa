package br.com.academiadigital.backend.trilha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.CursoRepository;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoOrdemRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoResponse;
import br.com.academiadigital.backend.trilha.dto.TrilhaRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaResponse;
import br.com.academiadigital.backend.trilha.mapper.TrilhaCursoMapper;
import br.com.academiadigital.backend.trilha.mapper.TrilhaMapper;

@ExtendWith(MockitoExtension.class)
class TrilhaServiceTest {

    @Mock
    private TrilhaRepository trilhaRepository;

    @Mock
    private TrilhaCursoRepository
            trilhaCursoRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private TrilhaMapper trilhaMapper;

    @Mock
    private TrilhaCursoMapper
            trilhaCursoMapper;

    @InjectMocks
    private TrilhaService trilhaService;

    @Test
    void deveCriarTrilha() {
        TrilhaRequest request =
                criarTrilhaRequest(
                        "Formação em Java"
                );

        Trilha trilha = criarTrilha(
                null,
                "Formação em Java"
        );

        Trilha trilhaSalva = criarTrilha(
                1L,
                "Formação em Java"
        );

        TrilhaResponse response =
                criarTrilhaResponse(
                        1L,
                        "Formação em Java"
                );

        when(trilhaRepository
                .existsByTituloIgnoreCase(
                        "Formação em Java"
                ))
                .thenReturn(false);

        when(trilhaMapper.toEntity(request))
                .thenReturn(trilha);

        when(trilhaRepository.save(trilha))
                .thenReturn(trilhaSalva);

        when(trilhaMapper.toResponse(
                trilhaSalva,
                List.of()
        )).thenReturn(response);

        TrilhaResponse resultado =
                trilhaService.criar(request);

        assertSame(response, resultado);

        verify(trilhaRepository)
                .save(trilha);
    }

    @Test
    void deveImpedirCriacaoComTituloDuplicado() {
        TrilhaRequest request =
                criarTrilhaRequest(
                        "Formação em Java"
                );

        when(trilhaRepository
                .existsByTituloIgnoreCase(
                        "Formação em Java"
                ))
                .thenReturn(true);

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> trilhaService.criar(
                                request
                        )
                );

        assertEquals(
                "Já existe uma trilha com "
                        + "o título informado.",
                excecao.getMessage()
        );

        verify(trilhaRepository, never())
                .save(any(Trilha.class));
    }

    @Test
    void deveListarTrilhasComFiltros() {
        Pageable pageable =
                PageRequest.of(0, 10);

        Trilha trilha = criarTrilha(
                1L,
                "Formação em Java"
        );

        TrilhaCurso trilhaCurso =
                criarTrilhaCurso(
                        10L,
                        trilha,
                        criarCurso(
                                2L,
                                "Java para iniciantes"
                        ),
                        1
                );

        TrilhaResponse response =
                criarTrilhaResponse(
                        1L,
                        "Formação em Java"
                );

        Page<Trilha> paginaEntidades =
                new PageImpl<>(
                        List.of(trilha),
                        pageable,
                        1
                );

        when(trilhaRepository.findAll(
                ArgumentMatchers
                        .<Specification<Trilha>>any(),
                eq(pageable)
        )).thenReturn(paginaEntidades);

        when(trilhaCursoRepository
                .findAllByTrilhaIdOrderByOrdemAsc(
                        1L
                ))
                .thenReturn(
                        List.of(trilhaCurso)
                );

        when(trilhaMapper.toResponse(
                trilha,
                List.of(trilhaCurso)
        )).thenReturn(response);

        Page<TrilhaResponse> resultado =
                trilhaService.listarTodos(
                        "Java",
                        StatusTrilha.PUBLICADA,
                        pageable
                );

        assertEquals(
                1,
                resultado.getTotalElements()
        );

        assertSame(
                response,
                resultado.getContent().get(0)
        );
    }

    @Test
    void deveLancarExcecaoQuandoTrilhaNaoExistir() {
        when(trilhaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> trilhaService
                                .buscarPorId(99L)
                );

        assertEquals(
                "Trilha não encontrada com o ID: 99",
                excecao.getMessage()
        );
    }

    @Test
    void deveAtualizarTrilha() {
        TrilhaRequest request =
                criarTrilhaRequest(
                        "Java atualizado"
                );

        request.setStatus(
                StatusTrilha.PUBLICADA
        );

        Trilha trilha = criarTrilha(
                1L,
                "Formação em Java"
        );

        Trilha trilhaSalva = criarTrilha(
                1L,
                "Java atualizado"
        );

        TrilhaResponse response =
                criarTrilhaResponse(
                        1L,
                        "Java atualizado"
                );

        when(trilhaRepository.findById(1L))
                .thenReturn(Optional.of(trilha));

        when(trilhaRepository
                .existsByTituloIgnoreCaseAndIdNot(
                        "Java atualizado",
                        1L
                ))
                .thenReturn(false);

        when(trilhaRepository.save(trilha))
                .thenReturn(trilhaSalva);

        when(trilhaCursoRepository
                .findAllByTrilhaIdOrderByOrdemAsc(
                        1L
                ))
                .thenReturn(List.of());

        when(trilhaMapper.toResponse(
                trilhaSalva,
                List.of()
        )).thenReturn(response);

        TrilhaResponse resultado =
                trilhaService.atualizar(
                        1L,
                        request
                );

        assertSame(response, resultado);

        verify(trilhaMapper)
                .atualizarEntity(
                        trilha,
                        request
                );
    }

    @Test
    void deveExcluirTrilhaESeusRelacionamentos() {
        Trilha trilha = criarTrilha(
                1L,
                "Formação em Java"
        );

        when(trilhaRepository.findById(1L))
                .thenReturn(Optional.of(trilha));

        trilhaService.excluir(1L);

        verify(trilhaCursoRepository)
                .deleteAllByTrilhaId(1L);

        verify(trilhaRepository)
                .deleteById(1L);
    }

    @Test
    void deveAdicionarCursoNaTrilha() {
        Trilha trilha = criarTrilha(
                1L,
                "Formação em Java"
        );

        Curso curso = criarCurso(
                2L,
                "Java para iniciantes"
        );

        TrilhaCursoRequest request =
                criarTrilhaCursoRequest(
                        2L,
                        1
                );

        TrilhaCurso trilhaCurso =
                criarTrilhaCurso(
                        null,
                        trilha,
                        curso,
                        1
                );

        TrilhaCurso trilhaCursoSalvo =
                criarTrilhaCurso(
                        10L,
                        trilha,
                        curso,
                        1
                );

        TrilhaCursoResponse response =
                criarTrilhaCursoResponse(
                        10L,
                        2L,
                        1
                );

        when(trilhaRepository.findById(1L))
                .thenReturn(Optional.of(trilha));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.of(curso));

        when(trilhaCursoRepository
                .existsByTrilhaIdAndCursoId(
                        1L,
                        2L
                ))
                .thenReturn(false);

        when(trilhaCursoRepository
                .existsByTrilhaIdAndOrdem(
                        1L,
                        1
                ))
                .thenReturn(false);

        when(trilhaCursoMapper.toEntity(
                request,
                trilha,
                curso
        )).thenReturn(trilhaCurso);

        when(trilhaCursoRepository
                .save(trilhaCurso))
                .thenReturn(trilhaCursoSalvo);

        when(trilhaCursoMapper.toResponse(
                trilhaCursoSalvo
        )).thenReturn(response);

        TrilhaCursoResponse resultado =
                trilhaService.adicionarCurso(
                        1L,
                        request
                );

        assertSame(response, resultado);
    }

    @Test
    void deveImpedirCursoDuplicadoNaTrilha() {
        Trilha trilha = criarTrilha(
                1L,
                "Formação em Java"
        );

        Curso curso = criarCurso(
                2L,
                "Java para iniciantes"
        );

        TrilhaCursoRequest request =
                criarTrilhaCursoRequest(
                        2L,
                        1
                );

        when(trilhaRepository.findById(1L))
                .thenReturn(Optional.of(trilha));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.of(curso));

        when(trilhaCursoRepository
                .existsByTrilhaIdAndCursoId(
                        1L,
                        2L
                ))
                .thenReturn(true);

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> trilhaService
                                .adicionarCurso(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "O curso já está associado "
                        + "a esta trilha.",
                excecao.getMessage()
        );

        verify(trilhaCursoRepository, never())
                .save(any(TrilhaCurso.class));
    }

    @Test
    void deveImpedirOrdemDuplicadaAoAdicionarCurso() {
        Trilha trilha = criarTrilha(
                1L,
                "Formação em Java"
        );

        Curso curso = criarCurso(
                2L,
                "Java para iniciantes"
        );

        TrilhaCursoRequest request =
                criarTrilhaCursoRequest(
                        2L,
                        1
                );

        when(trilhaRepository.findById(1L))
                .thenReturn(Optional.of(trilha));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.of(curso));

        when(trilhaCursoRepository
                .existsByTrilhaIdAndCursoId(
                        1L,
                        2L
                ))
                .thenReturn(false);

        when(trilhaCursoRepository
                .existsByTrilhaIdAndOrdem(
                        1L,
                        1
                ))
                .thenReturn(true);

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> trilhaService
                                .adicionarCurso(
                                        1L,
                                        request
                                )
                );

        assertEquals(
                "Já existe um curso na ordem "
                        + "informada para esta trilha.",
                excecao.getMessage()
        );

        verify(trilhaCursoRepository, never())
                .save(any(TrilhaCurso.class));
    }

    @Test
    void deveAtualizarOrdemDoCurso() {
        Trilha trilha = criarTrilha(
                1L,
                "Formação em Java"
        );

        Curso curso = criarCurso(
                2L,
                "Java para iniciantes"
        );

        TrilhaCurso trilhaCurso =
                criarTrilhaCurso(
                        10L,
                        trilha,
                        curso,
                        1
                );

        TrilhaCursoOrdemRequest request =
                new TrilhaCursoOrdemRequest();

        request.setOrdem(2);

        TrilhaCursoResponse response =
                criarTrilhaCursoResponse(
                        10L,
                        2L,
                        2
                );

        when(trilhaRepository.findById(1L))
                .thenReturn(Optional.of(trilha));

        when(trilhaCursoRepository
                .findByTrilhaIdAndCursoId(
                        1L,
                        2L
                ))
                .thenReturn(
                        Optional.of(trilhaCurso)
                );

        when(trilhaCursoRepository
                .existsByTrilhaIdAndOrdemAndIdNot(
                        1L,
                        2,
                        10L
                ))
                .thenReturn(false);

        when(trilhaCursoRepository
                .save(trilhaCurso))
                .thenReturn(trilhaCurso);

        when(trilhaCursoMapper.toResponse(
                trilhaCurso
        )).thenReturn(response);

        TrilhaCursoResponse resultado =
                trilhaService.atualizarOrdem(
                        1L,
                        2L,
                        request
                );

        assertSame(response, resultado);

        assertEquals(
                2,
                trilhaCurso.getOrdem()
        );
    }

    @Test
    void deveImpedirOrdemDuplicadaNaAtualizacao() {
        Trilha trilha = criarTrilha(
                1L,
                "Formação em Java"
        );

        Curso curso = criarCurso(
                2L,
                "Java para iniciantes"
        );

        TrilhaCurso trilhaCurso =
                criarTrilhaCurso(
                        10L,
                        trilha,
                        curso,
                        1
                );

        TrilhaCursoOrdemRequest request =
                new TrilhaCursoOrdemRequest();

        request.setOrdem(2);

        when(trilhaRepository.findById(1L))
                .thenReturn(Optional.of(trilha));

        when(trilhaCursoRepository
                .findByTrilhaIdAndCursoId(
                        1L,
                        2L
                ))
                .thenReturn(
                        Optional.of(trilhaCurso)
                );

        when(trilhaCursoRepository
                .existsByTrilhaIdAndOrdemAndIdNot(
                        1L,
                        2,
                        10L
                ))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> trilhaService.atualizarOrdem(
                        1L,
                        2L,
                        request
                )
        );

        verify(trilhaCursoRepository, never())
                .save(any(TrilhaCurso.class));
    }

    @Test
    void deveRemoverCursoDaTrilha() {
        Trilha trilha = criarTrilha(
                1L,
                "Formação em Java"
        );

        Curso curso = criarCurso(
                2L,
                "Java para iniciantes"
        );

        TrilhaCurso trilhaCurso =
                criarTrilhaCurso(
                        10L,
                        trilha,
                        curso,
                        1
                );

        when(trilhaRepository.findById(1L))
                .thenReturn(Optional.of(trilha));

        when(trilhaCursoRepository
                .findByTrilhaIdAndCursoId(
                        1L,
                        2L
                ))
                .thenReturn(
                        Optional.of(trilhaCurso)
                );

        trilhaService.removerCurso(
                1L,
                2L
        );

        verify(trilhaCursoRepository)
                .delete(trilhaCurso);
    }

    private TrilhaRequest criarTrilhaRequest(
            String titulo) {

        TrilhaRequest request =
                new TrilhaRequest();

        request.setTitulo(titulo);

        request.setDescricao(
                "Trilha completa de aprendizagem."
        );

        return request;
    }

    private Trilha criarTrilha(
            Long id,
            String titulo) {

        Trilha trilha = new Trilha();

        trilha.setId(id);
        trilha.setTitulo(titulo);
        trilha.setDescricao(
                "Trilha completa de aprendizagem."
        );
        trilha.setStatus(
                StatusTrilha.PUBLICADA
        );

        return trilha;
    }

    private Curso criarCurso(
            Long id,
            String titulo) {

        Curso curso = new Curso();

        curso.setId(id);
        curso.setTitulo(titulo);

        return curso;
    }

    private TrilhaCursoRequest criarTrilhaCursoRequest(
            Long cursoId,
            Integer ordem) {

        TrilhaCursoRequest request =
                new TrilhaCursoRequest();

        request.setCursoId(cursoId);
        request.setOrdem(ordem);

        return request;
    }

    private TrilhaCurso criarTrilhaCurso(
            Long id,
            Trilha trilha,
            Curso curso,
            Integer ordem) {

        TrilhaCurso trilhaCurso =
                new TrilhaCurso();

        trilhaCurso.setId(id);
        trilhaCurso.setTrilha(trilha);
        trilhaCurso.setCurso(curso);
        trilhaCurso.setOrdem(ordem);

        return trilhaCurso;
    }

    private TrilhaResponse criarTrilhaResponse(
            Long id,
            String titulo) {

        TrilhaResponse response =
                new TrilhaResponse();

        response.setId(id);
        response.setTitulo(titulo);
        response.setDescricao(
                "Trilha completa de aprendizagem."
        );
        response.setStatus(
                StatusTrilha.PUBLICADA
        );

        return response;
    }

    private TrilhaCursoResponse criarTrilhaCursoResponse(
            Long id,
            Long cursoId,
            Integer ordem) {

        TrilhaCursoResponse response =
                new TrilhaCursoResponse();

        response.setId(id);
        response.setCursoId(cursoId);
        response.setCursoTitulo(
                "Java para iniciantes"
        );
        response.setOrdem(ordem);

        return response;
    }
}