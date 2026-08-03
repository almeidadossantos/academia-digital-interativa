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

import br.com.academiadigital.backend.avaliacao.dto.QuestaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoUpdateRequest;
import br.com.academiadigital.backend.avaliacao.mapper.QuestaoMapper;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class QuestaoServiceTest {

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private QuestaoMapper questaoMapper;

    private QuestaoService questaoService;

    @BeforeEach
    void configurar() {
        questaoService = new QuestaoService(
                questaoRepository,
                avaliacaoRepository,
                questaoMapper
        );
    }

    @Test
    void deveCriarQuestao() {
        QuestaoRequest request = criarRequest();

        Avaliacao avaliacao = criarAvaliacao(
                1L,
                "Avaliação inicial"
        );

        Questao questao =
                criarQuestaoExistente(avaliacao);

        questao.setId(null);

        QuestaoResponse responseEsperada =
                criarResponse(10L);

        when(avaliacaoRepository.findById(1L))
                .thenReturn(Optional.of(avaliacao));

        when(questaoRepository
                .existsByAvaliacaoIdAndOrdem(
                        1L,
                        1
                ))
                .thenReturn(false);

        when(questaoMapper.toEntity(
                request,
                avaliacao
        )).thenReturn(questao);

        when(questaoRepository.save(questao))
                .thenAnswer(invocation -> {
                    questao.setId(10L);
                    return questao;
                });

        when(questaoMapper.toResponse(questao))
                .thenReturn(responseEsperada);

        QuestaoResponse response =
                questaoService.criar(request);

        assertEquals(10L, response.getId());

        assertEquals(
                "Qual componente executa as instruções?",
                response.getEnunciado()
        );

        verify(avaliacaoRepository)
                .findById(1L);

        verify(questaoRepository)
                .existsByAvaliacaoIdAndOrdem(
                        1L,
                        1
                );

        verify(questaoMapper).toEntity(
                request,
                avaliacao
        );

        verify(questaoRepository)
                .save(questao);

        verify(questaoMapper)
                .toResponse(questao);
    }

    @Test
    void deveLancarExcecaoAoCriarComAvaliacaoInexistente() {
        QuestaoRequest request = criarRequest();

        when(avaliacaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> questaoService.criar(request)
                );

        assertEquals(
                "Avaliação não encontrada com o ID: 1",
                exception.getMessage()
        );

        verifyNoInteractions(questaoRepository);
        verifyNoInteractions(questaoMapper);
    }

    @Test
    void deveLancarExcecaoAoCriarComOrdemDuplicada() {
        QuestaoRequest request = criarRequest();

        Avaliacao avaliacao = criarAvaliacao(
                1L,
                "Avaliação inicial"
        );

        when(avaliacaoRepository.findById(1L))
                .thenReturn(Optional.of(avaliacao));

        when(questaoRepository
                .existsByAvaliacaoIdAndOrdem(
                        1L,
                        1
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> questaoService.criar(request)
                );

        assertEquals(
                "Já existe uma questão "
                        + "com esta ordem na avaliação.",
                exception.getMessage()
        );

        verify(
                questaoMapper,
                never()
        ).toEntity(
                any(QuestaoRequest.class),
                any(Avaliacao.class)
        );

        verify(
                questaoRepository,
                never()
        ).save(any(Questao.class));
    }

    @Test
    void deveListarQuestoesComFiltrosEPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);

        Avaliacao avaliacao = criarAvaliacao(
                1L,
                "Avaliação inicial"
        );

        Questao questao =
                criarQuestaoExistente(avaliacao);

        Page<Questao> pagina =
                new PageImpl<>(
                        List.of(questao),
                        pageable,
                        1
                );

        QuestaoResponse response =
                criarResponse(10L);

        when(questaoRepository.findAll(
                any(Specification.class),
                eq(pageable)
        )).thenReturn(pagina);

        when(questaoMapper.toResponse(questao))
                .thenReturn(response);

        Page<QuestaoResponse> resultado =
                questaoService.listarTodos(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA,
                        "componente",
                        pageable
                );

        assertEquals(
                1,
                resultado.getTotalElements()
        );

        assertEquals(
                "Qual componente executa as instruções?",
                resultado.getContent()
                        .get(0)
                        .getEnunciado()
        );

        verify(questaoRepository).findAll(
                any(Specification.class),
                eq(pageable)
        );

        verify(questaoMapper)
                .toResponse(questao);
    }

    @Test
    void deveBuscarQuestaoPorId() {
        Avaliacao avaliacao = criarAvaliacao(
                1L,
                "Avaliação inicial"
        );

        Questao questao =
                criarQuestaoExistente(avaliacao);

        QuestaoResponse responseEsperada =
                criarResponse(10L);

        when(questaoRepository.findById(10L))
                .thenReturn(Optional.of(questao));

        when(questaoMapper.toResponse(questao))
                .thenReturn(responseEsperada);

        QuestaoResponse response =
                questaoService.buscarPorId(10L);

        assertEquals(10L, response.getId());

        assertEquals(
                TipoQuestao.MULTIPLA_ESCOLHA,
                response.getTipo()
        );

        verify(questaoRepository).findById(10L);

        verify(questaoMapper)
                .toResponse(questao);
    }

    @Test
    void deveLancarExcecaoAoBuscarQuestaoInexistente() {
        when(questaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> questaoService
                                .buscarPorId(99L)
                );

        assertEquals(
                "Questão não encontrada com o ID: 99",
                exception.getMessage()
        );

        verifyNoInteractions(questaoMapper);
    }

    @Test
    void deveAtualizarQuestao() {
        Avaliacao avaliacaoOriginal =
                criarAvaliacao(
                        1L,
                        "Avaliação original"
                );

        Avaliacao novaAvaliacao =
                criarAvaliacao(
                        2L,
                        "Avaliação atualizada"
                );

        Questao questao =
                criarQuestaoExistente(
                        avaliacaoOriginal
                );

        QuestaoUpdateRequest request =
                criarUpdateRequest();

        QuestaoResponse responseEsperada =
                criarResponse(10L);

        responseEsperada.setAvaliacaoId(2L);
        responseEsperada.setAvaliacaoTitulo(
                "Avaliação atualizada"
        );
        responseEsperada.setOrdem(3);

        when(questaoRepository.findById(10L))
                .thenReturn(Optional.of(questao));

        when(avaliacaoRepository.findById(2L))
                .thenReturn(Optional.of(novaAvaliacao));

        when(questaoRepository
                .existsByAvaliacaoIdAndOrdemAndIdNot(
                        2L,
                        3,
                        10L
                ))
                .thenReturn(false);

        when(questaoRepository.save(questao))
                .thenReturn(questao);

        when(questaoMapper.toResponse(questao))
                .thenReturn(responseEsperada);

        QuestaoResponse response =
                questaoService.atualizar(
                        10L,
                        request
                );

        assertEquals(10L, response.getId());
        assertEquals(2L, response.getAvaliacaoId());
        assertEquals(3, response.getOrdem());

        verify(questaoMapper).updateEntity(
                questao,
                request,
                novaAvaliacao
        );

        verify(questaoRepository)
                .existsByAvaliacaoIdAndOrdemAndIdNot(
                        2L,
                        3,
                        10L
                );

        verify(questaoRepository)
                .save(questao);

        verify(questaoMapper)
                .toResponse(questao);
    }

    @Test
    void deveLancarExcecaoAoAtualizarComOrdemDuplicada() {
        Avaliacao avaliacaoOriginal =
                criarAvaliacao(
                        1L,
                        "Avaliação original"
                );

        Avaliacao novaAvaliacao =
                criarAvaliacao(
                        2L,
                        "Nova avaliação"
                );

        Questao questao =
                criarQuestaoExistente(
                        avaliacaoOriginal
                );

        QuestaoUpdateRequest request =
                criarUpdateRequest();

        when(questaoRepository.findById(10L))
                .thenReturn(Optional.of(questao));

        when(avaliacaoRepository.findById(2L))
                .thenReturn(Optional.of(novaAvaliacao));

        when(questaoRepository
                .existsByAvaliacaoIdAndOrdemAndIdNot(
                        2L,
                        3,
                        10L
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> questaoService.atualizar(
                                10L,
                                request
                        )
                );

        assertEquals(
                "Já existe uma questão "
                        + "com esta ordem na avaliação.",
                exception.getMessage()
        );

        verify(
                questaoMapper,
                never()
        ).updateEntity(
                any(Questao.class),
                any(QuestaoUpdateRequest.class),
                any(Avaliacao.class)
        );

        verify(
                questaoRepository,
                never()
        ).save(any(Questao.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarQuestaoInexistente() {
        QuestaoUpdateRequest request =
                criarUpdateRequest();

        when(questaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> questaoService.atualizar(
                                99L,
                                request
                        )
                );

        assertEquals(
                "Questão não encontrada com o ID: 99",
                exception.getMessage()
        );

        verifyNoInteractions(avaliacaoRepository);
        verifyNoInteractions(questaoMapper);

        verify(
                questaoRepository,
                never()
        ).save(any(Questao.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComAvaliacaoInexistente() {
        Avaliacao avaliacaoOriginal =
                criarAvaliacao(
                        1L,
                        "Avaliação original"
                );

        Questao questao =
                criarQuestaoExistente(
                        avaliacaoOriginal
                );

        QuestaoUpdateRequest request =
                criarUpdateRequest();

        when(questaoRepository.findById(10L))
                .thenReturn(Optional.of(questao));

        when(avaliacaoRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> questaoService.atualizar(
                                10L,
                                request
                        )
                );

        assertEquals(
                "Avaliação não encontrada com o ID: 2",
                exception.getMessage()
        );

        verifyNoInteractions(questaoMapper);

        verify(
                questaoRepository,
                never()
        ).save(any(Questao.class));
    }

    @Test
    void deveExcluirQuestaoExistente() {
        Avaliacao avaliacao = criarAvaliacao(
                1L,
                "Avaliação inicial"
        );

        Questao questao =
                criarQuestaoExistente(avaliacao);

        when(questaoRepository.findById(10L))
                .thenReturn(Optional.of(questao));

        questaoService.excluir(10L);

        verify(questaoRepository)
                .delete(questao);
    }

    @Test
    void deveLancarExcecaoAoExcluirQuestaoInexistente() {
        when(questaoRepository.findById(80L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> questaoService.excluir(80L)
                );

        assertEquals(
                "Questão não encontrada com o ID: 80",
                exception.getMessage()
        );

        verify(
                questaoRepository,
                never()
        ).delete(any(Questao.class));
    }

    private QuestaoRequest criarRequest() {
        QuestaoRequest request =
                new QuestaoRequest();

        request.setAvaliacaoId(1L);

        request.setEnunciado(
                "Qual componente executa as instruções?"
        );

        request.setTipo(
                TipoQuestao.MULTIPLA_ESCOLHA
        );

        request.setOrdem(1);

        request.setPontuacao(
                new BigDecimal("2.50")
        );

        return request;
    }

    private QuestaoUpdateRequest criarUpdateRequest() {
        QuestaoUpdateRequest request =
                new QuestaoUpdateRequest();

        request.setAvaliacaoId(2L);

        request.setEnunciado(
                "Explique o funcionamento do processador."
        );

        request.setTipo(
                TipoQuestao.DISSERTATIVA
        );

        request.setOrdem(3);

        request.setPontuacao(
                new BigDecimal("4.00")
        );

        return request;
    }

    private Avaliacao criarAvaliacao(
            Long id,
            String titulo) {

        Avaliacao avaliacao =
                new Avaliacao();

        avaliacao.setId(id);
        avaliacao.setTitulo(titulo);

        return avaliacao;
    }

    private Questao criarQuestaoExistente(
            Avaliacao avaliacao) {

        Questao questao = new Questao();

        questao.setId(10L);
        questao.setAvaliacao(avaliacao);

        questao.setEnunciado(
                "Qual componente executa as instruções?"
        );

        questao.setTipo(
                TipoQuestao.MULTIPLA_ESCOLHA
        );

        questao.setOrdem(1);

        questao.setPontuacao(
                new BigDecimal("2.50")
        );

        return questao;
    }

    private QuestaoResponse criarResponse(Long id) {
        QuestaoResponse response =
                new QuestaoResponse();

        response.setId(id);
        response.setAvaliacaoId(1L);

        response.setAvaliacaoTitulo(
                "Avaliação inicial"
        );

        response.setEnunciado(
                "Qual componente executa as instruções?"
        );

        response.setTipo(
                TipoQuestao.MULTIPLA_ESCOLHA
        );

        response.setOrdem(1);

        response.setPontuacao(
                new BigDecimal("2.50")
        );

        return response;
    }
}
