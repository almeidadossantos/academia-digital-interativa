package br.com.academiadigital.backend.aula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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

import br.com.academiadigital.backend.aula.dto.AulaRequest;
import br.com.academiadigital.backend.aula.dto.AulaResponse;
import br.com.academiadigital.backend.aula.dto.AulaUpdateRequest;
import br.com.academiadigital.backend.aula.mapper.AulaMapper;
import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.CursoRepository;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AulaServiceTest {

    @Mock
    private AulaRepository aulaRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private AulaMapper aulaMapper;

    private AulaService aulaService;

    @BeforeEach
    void configurar() {
        aulaService = new AulaService(
                aulaRepository,
                cursoRepository,
                aulaMapper
        );
    }

    @Test
    void deveCriarAulaComStatusPadrao() {
        AulaRequest request = criarRequest();
        request.setStatus(null);

        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Aula aula = criarAulaExistente(curso);
        aula.setId(null);
        aula.setStatus(null);

        AulaResponse responseEsperada = criarResponse(
                10L,
                StatusAula.RASCUNHO
        );

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        when(aulaRepository.existsByCursoIdAndOrdem(
                1L,
                1
        )).thenReturn(false);

        when(aulaMapper.toEntity(request, curso))
                .thenReturn(aula);

        when(aulaRepository.save(aula))
                .thenAnswer(invocation -> {
                    aula.setId(10L);
                    return aula;
                });

        when(aulaMapper.toResponse(aula))
                .thenReturn(responseEsperada);

        AulaResponse response = aulaService.criar(request);

        assertEquals(10L, response.getId());
        assertEquals(
                StatusAula.RASCUNHO,
                response.getStatus()
        );
        assertEquals(
                StatusAula.RASCUNHO,
                aula.getStatus()
        );

        verify(cursoRepository).findById(1L);
        verify(aulaRepository)
                .existsByCursoIdAndOrdem(1L, 1);
        verify(aulaMapper).toEntity(request, curso);
        verify(aulaRepository).save(aula);
        verify(aulaMapper).toResponse(aula);
    }

    @Test
    void deveCriarAulaComStatusInformado() {
        AulaRequest request = criarRequest();
        request.setStatus(StatusAula.PUBLICADA);

        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Aula aula = criarAulaExistente(curso);
        aula.setStatus(StatusAula.PUBLICADA);

        AulaResponse responseEsperada = criarResponse(
                10L,
                StatusAula.PUBLICADA
        );

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        when(aulaRepository.existsByCursoIdAndOrdem(
                1L,
                1
        )).thenReturn(false);

        when(aulaMapper.toEntity(request, curso))
                .thenReturn(aula);

        when(aulaRepository.save(aula))
                .thenReturn(aula);

        when(aulaMapper.toResponse(aula))
                .thenReturn(responseEsperada);

        AulaResponse response = aulaService.criar(request);

        assertEquals(
                StatusAula.PUBLICADA,
                response.getStatus()
        );
        assertEquals(
                StatusAula.PUBLICADA,
                aula.getStatus()
        );
    }

    @Test
    void deveLancarExcecaoAoCriarAulaComCursoInexistente() {
        AulaRequest request = criarRequest();

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> aulaService.criar(request)
        );

        assertEquals(
                "Curso não encontrado com o ID: 1",
                exception.getMessage()
        );

        verifyNoInteractions(aulaRepository);
        verifyNoInteractions(aulaMapper);
    }

    @Test
    void deveLancarExcecaoAoCriarComOrdemDuplicada() {
        AulaRequest request = criarRequest();

        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        when(cursoRepository.findById(1L))
                .thenReturn(Optional.of(curso));

        when(aulaRepository.existsByCursoIdAndOrdem(
                1L,
                1
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aulaService.criar(request)
        );

        assertEquals(
                "Já existe uma aula com esta ordem no curso.",
                exception.getMessage()
        );

        verify(
                aulaMapper,
                never()
        ).toEntity(
                any(AulaRequest.class),
                any(Curso.class)
        );

        verify(
                aulaRepository,
                never()
        ).save(any(Aula.class));
    }

    @Test
    void deveListarAulasComFiltrosEPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);

        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Aula aula = criarAulaExistente(curso);

        Page<Aula> pagina = new PageImpl<>(
                List.of(aula),
                pageable,
                1
        );

        AulaResponse response = criarResponse(
                10L,
                StatusAula.PUBLICADA
        );

        when(aulaRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(pagina);

        when(aulaMapper.toResponse(aula))
                .thenReturn(response);

        Page<AulaResponse> resultado =
                aulaService.listarTodos(
                        1L,
                        StatusAula.PUBLICADA,
                        "computador",
                        pageable
                );

        assertEquals(1, resultado.getTotalElements());
        assertEquals(
                "Introdução ao computador",
                resultado.getContent().get(0).getTitulo()
        );

        verify(aulaRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verify(aulaMapper).toResponse(aula);
    }

    @Test
    void deveBuscarAulaPorId() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Aula aula = criarAulaExistente(curso);

        AulaResponse responseEsperada = criarResponse(
                10L,
                StatusAula.PUBLICADA
        );

        when(aulaRepository.findById(10L))
                .thenReturn(Optional.of(aula));

        when(aulaMapper.toResponse(aula))
                .thenReturn(responseEsperada);

        AulaResponse response =
                aulaService.buscarPorId(10L);

        assertEquals(10L, response.getId());
        assertEquals(
                "Introdução ao computador",
                response.getTitulo()
        );

        verify(aulaRepository).findById(10L);
        verify(aulaMapper).toResponse(aula);
    }

    @Test
    void deveLancarExcecaoAoBuscarAulaInexistente() {
        when(aulaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> aulaService.buscarPorId(99L)
        );

        assertEquals(
                "Aula não encontrada com o ID: 99",
                exception.getMessage()
        );

        verifyNoInteractions(aulaMapper);
    }

    @Test
    void deveAtualizarAula() {
        Curso cursoOriginal = criarCurso(
                1L,
                "Curso original"
        );

        Curso novoCurso = criarCurso(
                2L,
                "Curso atualizado"
        );

        Aula aula = criarAulaExistente(cursoOriginal);
        AulaUpdateRequest request = criarUpdateRequest();

        AulaResponse responseEsperada = criarResponse(
                10L,
                StatusAula.PUBLICADA
        );
        responseEsperada.setCursoId(2L);
        responseEsperada.setCursoTitulo("Curso atualizado");
        responseEsperada.setOrdem(3);

        when(aulaRepository.findById(10L))
                .thenReturn(Optional.of(aula));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.of(novoCurso));

        when(
                aulaRepository
                        .existsByCursoIdAndOrdemAndIdNot(
                                2L,
                                3,
                                10L
                        )
        ).thenReturn(false);

        when(aulaRepository.save(aula))
                .thenReturn(aula);

        when(aulaMapper.toResponse(aula))
                .thenReturn(responseEsperada);

        AulaResponse response =
                aulaService.atualizar(10L, request);

        assertEquals(10L, response.getId());
        assertEquals(2L, response.getCursoId());
        assertEquals(3, response.getOrdem());

        verify(aulaMapper).updateEntity(
                aula,
                request,
                novoCurso
        );

        verify(
                aulaRepository
        ).existsByCursoIdAndOrdemAndIdNot(
                2L,
                3,
                10L
        );

        verify(aulaRepository).save(aula);
        verify(aulaMapper).toResponse(aula);
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

        Aula aula = criarAulaExistente(cursoOriginal);
        AulaUpdateRequest request = criarUpdateRequest();

        when(aulaRepository.findById(10L))
                .thenReturn(Optional.of(aula));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.of(novoCurso));

        when(
                aulaRepository
                        .existsByCursoIdAndOrdemAndIdNot(
                                2L,
                                3,
                                10L
                        )
        ).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> aulaService.atualizar(10L, request)
        );

        assertEquals(
                "Já existe uma aula com esta ordem no curso.",
                exception.getMessage()
        );

        verify(
                aulaMapper,
                never()
        ).updateEntity(
                any(Aula.class),
                any(AulaUpdateRequest.class),
                any(Curso.class)
        );

        verify(
                aulaRepository,
                never()
        ).save(any(Aula.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarAulaInexistente() {
        AulaUpdateRequest request = criarUpdateRequest();

        when(aulaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> aulaService.atualizar(99L, request)
        );

        assertEquals(
                "Aula não encontrada com o ID: 99",
                exception.getMessage()
        );

        verifyNoInteractions(cursoRepository);
        verifyNoInteractions(aulaMapper);

        verify(
                aulaRepository,
                never()
        ).save(any(Aula.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComCursoInexistente() {
        Curso cursoOriginal = criarCurso(
                1L,
                "Curso original"
        );

        Aula aula = criarAulaExistente(cursoOriginal);
        AulaUpdateRequest request = criarUpdateRequest();

        when(aulaRepository.findById(10L))
                .thenReturn(Optional.of(aula));

        when(cursoRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> aulaService.atualizar(10L, request)
        );

        assertEquals(
                "Curso não encontrado com o ID: 2",
                exception.getMessage()
        );

        verifyNoInteractions(aulaMapper);

        verify(
                aulaRepository,
                never()
        ).save(any(Aula.class));
    }

    @Test
    void deveExcluirAulaExistente() {
        Curso curso = criarCurso(
                1L,
                "Informática Básica"
        );

        Aula aula = criarAulaExistente(curso);

        when(aulaRepository.findById(10L))
                .thenReturn(Optional.of(aula));

        aulaService.excluir(10L);

        verify(aulaRepository).delete(aula);
    }

    @Test
    void deveLancarExcecaoAoExcluirAulaInexistente() {
        when(aulaRepository.findById(80L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> aulaService.excluir(80L)
        );

        assertEquals(
                "Aula não encontrada com o ID: 80",
                exception.getMessage()
        );

        verify(
                aulaRepository,
                never()
        ).delete(any(Aula.class));
    }

    private AulaRequest criarRequest() {
        AulaRequest request = new AulaRequest();

        request.setCursoId(1L);
        request.setTitulo("Introdução ao computador");
        request.setDescricao(
                "Conhecendo os componentes básicos do computador."
        );
        request.setOrdem(1);
        request.setDuracaoMinutos(30);
        request.setVideoUrl(
                "https://exemplo.com/videos/aula-1"
        );

        return request;
    }

    private AulaUpdateRequest criarUpdateRequest() {
        AulaUpdateRequest request =
                new AulaUpdateRequest();

        request.setCursoId(2L);
        request.setTitulo("Aula atualizada");
        request.setDescricao(
                "Descrição atualizada da aula."
        );
        request.setOrdem(3);
        request.setDuracaoMinutos(45);
        request.setVideoUrl(
                "https://exemplo.com/videos/aula-atualizada"
        );
        request.setStatus(StatusAula.PUBLICADA);

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

    private Aula criarAulaExistente(Curso curso) {
        Aula aula = new Aula();

        aula.setId(10L);
        aula.setCurso(curso);
        aula.setTitulo("Introdução ao computador");
        aula.setDescricao(
                "Conhecendo os componentes básicos do computador."
        );
        aula.setOrdem(1);
        aula.setDuracaoMinutos(30);
        aula.setVideoUrl(
                "https://exemplo.com/videos/aula-1"
        );
        aula.setStatus(StatusAula.PUBLICADA);

        return aula;
    }

    private AulaResponse criarResponse(
            Long id,
            StatusAula status) {

        AulaResponse response = new AulaResponse();

        response.setId(id);
        response.setCursoId(1L);
        response.setCursoTitulo("Informática Básica");
        response.setTitulo("Introdução ao computador");
        response.setDescricao(
                "Conhecendo os componentes básicos do computador."
        );
        response.setOrdem(1);
        response.setDuracaoMinutos(30);
        response.setVideoUrl(
                "https://exemplo.com/videos/aula-1"
        );
        response.setStatus(status);

        return response;
    }
}