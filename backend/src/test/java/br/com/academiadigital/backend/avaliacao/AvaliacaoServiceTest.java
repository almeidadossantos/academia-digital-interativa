package br.com.academiadigital.backend.avaliacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoUpdateRequest;
import br.com.academiadigital.backend.avaliacao.mapper.AvaliacaoMapper;
import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.CursoRepository;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private AvaliacaoMapper avaliacaoMapper;

    private AvaliacaoService avaliacaoService;

    @BeforeEach
    void configurar() {
        avaliacaoService = new AvaliacaoService(
                avaliacaoRepository,
                cursoRepository,
                avaliacaoMapper
        );
    }

    @Test
    void deveCriarAvaliacaoComStatusPadrao() {
        AvaliacaoRequest request = criarRequest();
        request.setStatus(null);

        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Avaliacao avaliacao =
                criarAvaliacaoExistente(curso);

        avaliacao.setId(null);
        avaliacao.setStatus(null);

        AvaliacaoResponse responseEsperada =
                criarResponse(
                        10L,
                        StatusAvaliacao.RASCUNHO
                );

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        when(avaliacaoRepository
                .existsByCursoIdAndOrdem(
                        1L,
                        1
                ))
                .thenReturn(false);

        when(avaliacaoMapper.toEntity(
                request,
                curso
        )).thenReturn(avaliacao);

        when(avaliacaoRepository.save(avaliacao))
                .thenAnswer(invocation -> {
                    avaliacao.setId(10L);
                    return avaliacao;
                });

        when(avaliacaoMapper.toResponse(avaliacao))
                .thenReturn(responseEsperada);

        AvaliacaoResponse response =
                avaliacaoService.criar(request);

        assertEquals(10L, response.getId());

        assertEquals(
                StatusAvaliacao.RASCUNHO,
                response.getStatus()
        );

        assertEquals(
                StatusAvaliacao.RASCUNHO,
                avaliacao.getStatus()
        );

        verify(cursoRepository).findById(1L);

        verify(avaliacaoRepository)
                .existsByCursoIdAndOrdem(
                        1L,
                        1
                );

        verify(avaliacaoMapper).toEntity(
                request,
                curso
        );

        verify(avaliacaoRepository)
                .save(avaliacao);

        verify(avaliacaoMapper)
                .toResponse(avaliacao);
    }

    @Test
    void deveCriarAvaliacaoComStatusInformado() {
        AvaliacaoRequest request = criarRequest();

        request.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Avaliacao avaliacao =
                criarAvaliacaoExistente(curso);

        avaliacao.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        AvaliacaoResponse responseEsperada =
                criarResponse(
                        10L,
                        StatusAvaliacao.PUBLICADA
                );

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        when(avaliacaoRepository
                .existsByCursoIdAndOrdem(
                        1L,
                        1
                ))
                .thenReturn(false);

        when(avaliacaoMapper.toEntity(
                request,
                curso
        )).thenReturn(avaliacao);

        when(avaliacaoRepository.save(avaliacao))
                .thenReturn(avaliacao);

        when(avaliacaoMapper.toResponse(avaliacao))
                .thenReturn(responseEsperada);

        AvaliacaoResponse response =
                avaliacaoService.criar(request);

        assertEquals(
                StatusAvaliacao.PUBLICADA,
                response.getStatus()
        );

        assertEquals(
                StatusAvaliacao.PUBLICADA,
                avaliacao.getStatus()
        );
    }

    @Test
    void deveLancarExcecaoAoCriarComCursoInexistente() {
        AvaliacaoRequest request = criarRequest();

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> avaliacaoService.criar(request)
                );

        assertEquals(
                "Curso não encontrado com o ID: 1",
                exception.getMessage()
        );

        verifyNoInteractions(avaliacaoRepository);
        verifyNoInteractions(avaliacaoMapper);
    }

    @Test
    void deveLancarExcecaoAoCriarComOrdemDuplicada() {
        AvaliacaoRequest request = criarRequest();

        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        when(avaliacaoRepository
                .existsByCursoIdAndOrdem(
                        1L,
                        1
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> avaliacaoService.criar(request)
                );

        assertEquals(
                "Já existe uma avaliação "
                        + "com esta ordem no curso.",
                exception.getMessage()
        );

        verify(
                avaliacaoMapper,
                never()
        ).toEntity(
                any(AvaliacaoRequest.class),
                any(Curso.class)
        );

        verify(
                avaliacaoRepository,
                never()
        ).save(any(Avaliacao.class));
    }

    @Test
    void deveListarAvaliacoesComFiltrosEPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);

        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Avaliacao avaliacao =
                criarAvaliacaoExistente(curso);

        Page<Avaliacao> pagina =
                new PageImpl<>(
                        List.of(avaliacao),
                        pageable,
                        1
                );

        AvaliacaoResponse response =
                criarResponse(
                        10L,
                        StatusAvaliacao.PUBLICADA
                );

        when(avaliacaoRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(pagina);

        when(avaliacaoMapper.toResponse(avaliacao))
                .thenReturn(response);

        Page<AvaliacaoResponse> resultado =
                avaliacaoService.listarTodos(
                        1L,
                        StatusAvaliacao.PUBLICADA,
                        "conhecimentos",
                        pageable
                );

        assertEquals(
                1,
                resultado.getTotalElements()
        );

        assertEquals(
                "Avaliação de conhecimentos básicos",
                resultado.getContent()
                        .get(0)
                        .getTitulo()
        );

        verify(avaliacaoRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verify(avaliacaoMapper)
                .toResponse(avaliacao);
    }

    @Test
    void deveBuscarAvaliacaoPorId() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Avaliacao avaliacao =
                criarAvaliacaoExistente(curso);

        AvaliacaoResponse responseEsperada =
                criarResponse(
                        10L,
                        StatusAvaliacao.PUBLICADA
                );

        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));

        when(avaliacaoMapper.toResponse(avaliacao))
                .thenReturn(responseEsperada);

        AvaliacaoResponse response =
                avaliacaoService.buscarPorId(10L);

        assertEquals(10L, response.getId());

        assertEquals(
                "Avaliação de conhecimentos básicos",
                response.getTitulo()
        );

        verify(avaliacaoRepository).findById(10L);

        verify(avaliacaoMapper)
                .toResponse(avaliacao);
    }

    @Test
    void deveLancarExcecaoAoBuscarAvaliacaoInexistente() {
        when(avaliacaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> avaliacaoService
                                .buscarPorId(99L)
                );

        assertEquals(
                "Avaliação não encontrada com o ID: 99",
                exception.getMessage()
        );

        verifyNoInteractions(avaliacaoMapper);
    }

    @Test
    void deveAtualizarAvaliacao() {
        Curso cursoOriginal = criarCurso(
                1L,
                "Curso original"
        );

        Curso novoCurso = criarCurso(
                2L,
                "Curso atualizado"
        );

        Avaliacao avaliacao =
                criarAvaliacaoExistente(
                        cursoOriginal
                );

        AvaliacaoUpdateRequest request =
                criarUpdateRequest();

        AvaliacaoResponse responseEsperada =
                criarResponse(
                        10L,
                        StatusAvaliacao.PUBLICADA
                );

        responseEsperada.setCursoId(2L);
        responseEsperada.setCursoTitulo(
                "Curso atualizado"
        );
        responseEsperada.setOrdem(3);

        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.of(novoCurso));

        when(
                avaliacaoRepository
                        .existsByCursoIdAndOrdemAndIdNot(
                                2L,
                                3,
                                10L
                        )
        ).thenReturn(false);

        when(avaliacaoRepository.save(avaliacao))
                .thenReturn(avaliacao);

        when(avaliacaoMapper.toResponse(avaliacao))
                .thenReturn(responseEsperada);

        AvaliacaoResponse response =
                avaliacaoService.atualizar(
                        10L,
                        request
                );

        assertEquals(10L, response.getId());
        assertEquals(2L, response.getCursoId());
        assertEquals(3, response.getOrdem());

        verify(avaliacaoMapper).updateEntity(
                avaliacao,
                request,
                novoCurso
        );

        verify(avaliacaoRepository)
                .existsByCursoIdAndOrdemAndIdNot(
                        2L,
                        3,
                        10L
                );

        verify(avaliacaoRepository)
                .save(avaliacao);

        verify(avaliacaoMapper)
                .toResponse(avaliacao);
    }

    @Test
    void deveLancarExcecaoAoAtualizarComOrdemDuplicada() {
        Curso cursoOriginal = criarCurso(
                1L,
                "Curso original"
        );

        Curso novoCurso = criarCurso(
                2L,
                "Novo curso"
        );

        Avaliacao avaliacao =
                criarAvaliacaoExistente(
                        cursoOriginal
                );

        AvaliacaoUpdateRequest request =
                criarUpdateRequest();

        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.of(novoCurso));

        when(
                avaliacaoRepository
                        .existsByCursoIdAndOrdemAndIdNot(
                                2L,
                                3,
                                10L
                        )
        ).thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> avaliacaoService.atualizar(
                                10L,
                                request
                        )
                );

        assertEquals(
                "Já existe uma avaliação "
                        + "com esta ordem no curso.",
                exception.getMessage()
        );

        verify(
                avaliacaoMapper,
                never()
        ).updateEntity(
                any(Avaliacao.class),
                any(AvaliacaoUpdateRequest.class),
                any(Curso.class)
        );

        verify(
                avaliacaoRepository,
                never()
        ).save(any(Avaliacao.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarAvaliacaoInexistente() {
        AvaliacaoUpdateRequest request =
                criarUpdateRequest();

        when(avaliacaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> avaliacaoService.atualizar(
                                99L,
                                request
                        )
                );

        assertEquals(
                "Avaliação não encontrada com o ID: 99",
                exception.getMessage()
        );

        verifyNoInteractions(cursoRepository);
        verifyNoInteractions(avaliacaoMapper);

        verify(
                avaliacaoRepository,
                never()
        ).save(any(Avaliacao.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComCursoInexistente() {
        Curso cursoOriginal = criarCurso(
                1L,
                "Curso original"
        );

        Avaliacao avaliacao =
                criarAvaliacaoExistente(
                        cursoOriginal
                );

        AvaliacaoUpdateRequest request =
                criarUpdateRequest();

        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> avaliacaoService.atualizar(
                                10L,
                                request
                        )
                );

        assertEquals(
                "Curso não encontrado com o ID: 2",
                exception.getMessage()
        );

        verifyNoInteractions(avaliacaoMapper);

        verify(
                avaliacaoRepository,
                never()
        ).save(any(Avaliacao.class));
    }

    @Test
    void deveExcluirAvaliacaoExistente() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Avaliacao avaliacao =
                criarAvaliacaoExistente(curso);

        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));

        avaliacaoService.excluir(10L);

        verify(avaliacaoRepository)
                .delete(avaliacao);
    }

    @Test
    void deveLancarExcecaoAoExcluirAvaliacaoInexistente() {
        when(avaliacaoRepository.findById(80L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> avaliacaoService.excluir(80L)
                );

        assertEquals(
                "Avaliação não encontrada com o ID: 80",
                exception.getMessage()
        );

        verify(
                avaliacaoRepository,
                never()
        ).delete(any(Avaliacao.class));
    }

    private AvaliacaoRequest criarRequest() {
        AvaliacaoRequest request =
                new AvaliacaoRequest();

        request.setCursoId(1L);

        request.setTitulo(
                "Avaliação de conhecimentos básicos"
        );

        request.setDescricao(
                "Avaliação dos conteúdos iniciais do curso."
        );

        request.setOrdem(1);

        request.setNotaMinima(
                new BigDecimal("7.00")
        );

        request.setMaximoTentativas(3);
        request.setTempoLimiteMinutos(60);

        return request;
    }

    private AvaliacaoUpdateRequest criarUpdateRequest() {
        AvaliacaoUpdateRequest request =
                new AvaliacaoUpdateRequest();

        request.setCursoId(2L);
        request.setTitulo(
                "Avaliação atualizada"
        );

        request.setDescricao(
                "Descrição atualizada da avaliação."
        );

        request.setOrdem(3);

        request.setNotaMinima(
                new BigDecimal("8.00")
        );

        request.setMaximoTentativas(4);
        request.setTempoLimiteMinutos(90);

        request.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        return request;
    }

    private Curso criarCurso(
            Long id,
            String titulo) {

        Curso curso = new Curso();

        curso.setId(id);
        curso.setTitulo(titulo);

        return curso;
    }

    private Avaliacao criarAvaliacaoExistente(
            Curso curso) {

        Avaliacao avaliacao =
                new Avaliacao();

        avaliacao.setId(10L);
        avaliacao.setCurso(curso);

        avaliacao.setTitulo(
                "Avaliação de conhecimentos básicos"
        );

        avaliacao.setDescricao(
                "Avaliação dos conteúdos iniciais do curso."
        );

        avaliacao.setOrdem(1);

        avaliacao.setNotaMinima(
                new BigDecimal("7.00")
        );

        avaliacao.setMaximoTentativas(3);
        avaliacao.setTempoLimiteMinutos(60);

        avaliacao.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        return avaliacao;
    }

    private AvaliacaoResponse criarResponse(
            Long id,
            StatusAvaliacao status) {

        AvaliacaoResponse response =
                new AvaliacaoResponse();

        response.setId(id);
        response.setCursoId(1L);

        response.setCursoTitulo(
                "Informática Básica"
        );

        response.setTitulo(
                "Avaliação de conhecimentos básicos"
        );

        response.setDescricao(
                "Avaliação dos conteúdos iniciais do curso."
        );

        response.setOrdem(1);

        response.setNotaMinima(
                new BigDecimal("7.00")
        );

        response.setMaximoTentativas(3);
        response.setTempoLimiteMinutos(60);
        response.setStatus(status);

        return response;
    }
}
