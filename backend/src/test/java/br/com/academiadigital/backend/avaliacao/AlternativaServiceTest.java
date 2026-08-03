package br.com.academiadigital.backend.avaliacao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

import br.com.academiadigital.backend.avaliacao.dto.AlternativaRequest;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaResponse;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaUpdateRequest;
import br.com.academiadigital.backend.avaliacao.mapper.AlternativaMapper;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class AlternativaServiceTest {

    @Mock
    private AlternativaRepository alternativaRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private AlternativaMapper alternativaMapper;

    private AlternativaService alternativaService;

    @BeforeEach
    void configurar() {
        alternativaService =
                new AlternativaService(
                        alternativaRepository,
                        questaoRepository,
                        alternativaMapper
                );
    }

    @Test
    void deveCriarAlternativa() {
        AlternativaRequest request =
                criarRequest();

        Questao questao =
                criarQuestao(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        Alternativa alternativa =
                criarAlternativa(questao);

        alternativa.setId(null);

        AlternativaResponse responseEsperada =
                criarResponse(
                        10L,
                        1L,
                        "Processador",
                        true,
                        1
                );

        when(questaoRepository.findById(1L))
                .thenReturn(Optional.of(questao));

        when(alternativaRepository
                .existsByQuestaoIdAndOrdem(
                        1L,
                        1
                ))
                .thenReturn(false);

        when(alternativaMapper.toEntity(
                request,
                questao
        )).thenReturn(alternativa);

        when(alternativaRepository.save(alternativa))
                .thenAnswer(invocation -> {
                    alternativa.setId(10L);
                    return alternativa;
                });

        when(alternativaMapper.toResponse(alternativa))
                .thenReturn(responseEsperada);

        AlternativaResponse response =
                alternativaService.criar(request);

        assertEquals(
                10L,
                response.getId()
        );

        assertEquals(
                "Processador",
                response.getTexto()
        );

        assertEquals(
                true,
                response.getCorreta()
        );

        verify(questaoRepository)
                .findById(1L);

        verify(alternativaRepository)
                .existsByQuestaoIdAndOrdem(
                        1L,
                        1
                );

        verify(alternativaMapper)
                .toEntity(
                        request,
                        questao
                );

        verify(alternativaRepository)
                .save(alternativa);

        verify(alternativaMapper)
                .toResponse(alternativa);
    }

    @Test
    void deveLancarExcecaoAoCriarComQuestaoInexistente() {
        AlternativaRequest request =
                criarRequest();

        when(questaoRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> alternativaService.criar(request)
                );

        assertEquals(
                "Questão não encontrada com o ID: 1",
                exception.getMessage()
        );

        verifyNoInteractions(
                alternativaRepository,
                alternativaMapper
        );
    }

    @Test
    void deveLancarExcecaoAoCriarParaQuestaoDissertativa() {
        AlternativaRequest request =
                criarRequest();

        Questao questao =
                criarQuestao(
                        1L,
                        TipoQuestao.DISSERTATIVA
                );

        when(questaoRepository.findById(1L))
                .thenReturn(Optional.of(questao));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> alternativaService.criar(request)
                );

        assertEquals(
                "Questões dissertativas "
                        + "não podem possuir alternativas.",
                exception.getMessage()
        );

        verifyNoInteractions(
                alternativaRepository,
                alternativaMapper
        );
    }

    @Test
    void deveLancarExcecaoAoCriarComOrdemDuplicada() {
        AlternativaRequest request =
                criarRequest();

        Questao questao =
                criarQuestao(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        when(questaoRepository.findById(1L))
                .thenReturn(Optional.of(questao));

        when(alternativaRepository
                .existsByQuestaoIdAndOrdem(
                        1L,
                        1
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> alternativaService.criar(request)
                );

        assertEquals(
                "Já existe uma alternativa "
                        + "com esta ordem na questão.",
                exception.getMessage()
        );

        verify(
                alternativaMapper,
                never()
        ).toEntity(
                any(AlternativaRequest.class),
                any(Questao.class)
        );

        verify(
                alternativaRepository,
                never()
        ).save(any(Alternativa.class));
    }

    @Test
    void deveListarAlternativasOrdenadasPorQuestao() {
        Questao questao =
                criarQuestao(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        Alternativa primeiraAlternativa =
                criarAlternativa(questao);

        primeiraAlternativa.setId(10L);

        Alternativa segundaAlternativa =
                criarAlternativa(questao);

        segundaAlternativa.setId(11L);
        segundaAlternativa.setTexto(
                "Memória RAM"
        );
        segundaAlternativa.setCorreta(false);
        segundaAlternativa.setOrdem(2);

        AlternativaResponse primeiraResponse =
                criarResponse(
                        10L,
                        1L,
                        "Processador",
                        true,
                        1
                );

        AlternativaResponse segundaResponse =
                criarResponse(
                        11L,
                        1L,
                        "Memória RAM",
                        false,
                        2
                );

        when(questaoRepository.findById(1L))
                .thenReturn(Optional.of(questao));

        when(alternativaRepository
                .findAllByQuestaoIdOrderByOrdemAsc(1L))
                .thenReturn(
                        List.of(
                                primeiraAlternativa,
                                segundaAlternativa
                        )
                );

        when(alternativaMapper.toResponse(
                primeiraAlternativa
        )).thenReturn(primeiraResponse);

        when(alternativaMapper.toResponse(
                segundaAlternativa
        )).thenReturn(segundaResponse);

        List<AlternativaResponse> resultado =
                alternativaService
                        .listarPorQuestao(1L);

        assertEquals(
                2,
                resultado.size()
        );

        assertEquals(
                1,
                resultado.get(0).getOrdem()
        );

        assertEquals(
                2,
                resultado.get(1).getOrdem()
        );

        verify(alternativaRepository)
                .findAllByQuestaoIdOrderByOrdemAsc(1L);

        verify(alternativaMapper)
                .toResponse(primeiraAlternativa);

        verify(alternativaMapper)
                .toResponse(segundaAlternativa);
    }

    @Test
    void deveLancarExcecaoAoListarQuestaoInexistente() {
        when(questaoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> alternativaService
                                .listarPorQuestao(99L)
                );

        assertEquals(
                "Questão não encontrada com o ID: 99",
                exception.getMessage()
        );

        verifyNoInteractions(
                alternativaRepository,
                alternativaMapper
        );
    }

    @Test
    void deveBuscarAlternativaPorId() {
        Questao questao =
                criarQuestao(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        Alternativa alternativa =
                criarAlternativa(questao);

        AlternativaResponse responseEsperada =
                criarResponse(
                        10L,
                        1L,
                        "Processador",
                        true,
                        1
                );

        when(alternativaRepository.findById(10L))
                .thenReturn(Optional.of(alternativa));

        when(alternativaMapper.toResponse(alternativa))
                .thenReturn(responseEsperada);

        AlternativaResponse response =
                alternativaService.buscarPorId(10L);

        assertEquals(
                10L,
                response.getId()
        );

        assertEquals(
                "Processador",
                response.getTexto()
        );

        verify(alternativaRepository)
                .findById(10L);

        verify(alternativaMapper)
                .toResponse(alternativa);
    }

    @Test
    void deveLancarExcecaoAoBuscarAlternativaInexistente() {
        when(alternativaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> alternativaService
                                .buscarPorId(99L)
                );

        assertEquals(
                "Alternativa não encontrada com o ID: 99",
                exception.getMessage()
        );

        verifyNoInteractions(alternativaMapper);
    }

    @Test
    void deveAtualizarAlternativa() {
        Questao questaoOriginal =
                criarQuestao(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        Questao novaQuestao =
                criarQuestao(
                        2L,
                        TipoQuestao.VERDADEIRO_FALSO
                );

        Alternativa alternativa =
                criarAlternativa(questaoOriginal);

        AlternativaUpdateRequest request =
                criarUpdateRequest();

        AlternativaResponse responseEsperada =
                criarResponse(
                        10L,
                        2L,
                        "Memória RAM",
                        false,
                        3
                );

        when(alternativaRepository.findById(10L))
                .thenReturn(Optional.of(alternativa));

        when(questaoRepository.findById(2L))
                .thenReturn(Optional.of(novaQuestao));

        when(alternativaRepository
                .existsByQuestaoIdAndOrdemAndIdNot(
                        2L,
                        3,
                        10L
                ))
                .thenReturn(false);

        when(alternativaRepository.save(alternativa))
                .thenReturn(alternativa);

        when(alternativaMapper.toResponse(alternativa))
                .thenReturn(responseEsperada);

        AlternativaResponse response =
                alternativaService.atualizar(
                        10L,
                        request
                );

        assertEquals(
                10L,
                response.getId()
        );

        assertEquals(
                2L,
                response.getQuestaoId()
        );

        assertEquals(
                3,
                response.getOrdem()
        );

        verify(alternativaMapper)
                .updateEntity(
                        alternativa,
                        request,
                        novaQuestao
                );

        verify(alternativaRepository)
                .existsByQuestaoIdAndOrdemAndIdNot(
                        2L,
                        3,
                        10L
                );

        verify(alternativaRepository)
                .save(alternativa);

        verify(alternativaMapper)
                .toResponse(alternativa);
    }

    @Test
    void deveLancarExcecaoAoAtualizarAlternativaInexistente() {
        AlternativaUpdateRequest request =
                criarUpdateRequest();

        when(alternativaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> alternativaService.atualizar(
                                99L,
                                request
                        )
                );

        assertEquals(
                "Alternativa não encontrada com o ID: 99",
                exception.getMessage()
        );

        verifyNoInteractions(
                questaoRepository,
                alternativaMapper
        );

        verify(
                alternativaRepository,
                never()
        ).save(any(Alternativa.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComQuestaoInexistente() {
        Questao questaoOriginal =
                criarQuestao(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        Alternativa alternativa =
                criarAlternativa(questaoOriginal);

        AlternativaUpdateRequest request =
                criarUpdateRequest();

        when(alternativaRepository.findById(10L))
                .thenReturn(Optional.of(alternativa));

        when(questaoRepository.findById(2L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> alternativaService.atualizar(
                                10L,
                                request
                        )
                );

        assertEquals(
                "Questão não encontrada com o ID: 2",
                exception.getMessage()
        );

        verifyNoInteractions(alternativaMapper);

        verify(
                alternativaRepository,
                never()
        ).save(any(Alternativa.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarParaQuestaoDissertativa() {
        Questao questaoOriginal =
                criarQuestao(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        Questao questaoDissertativa =
                criarQuestao(
                        2L,
                        TipoQuestao.DISSERTATIVA
                );

        Alternativa alternativa =
                criarAlternativa(questaoOriginal);

        AlternativaUpdateRequest request =
                criarUpdateRequest();

        when(alternativaRepository.findById(10L))
                .thenReturn(Optional.of(alternativa));

        when(questaoRepository.findById(2L))
                .thenReturn(Optional.of(questaoDissertativa));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> alternativaService.atualizar(
                                10L,
                                request
                        )
                );

        assertEquals(
                "Questões dissertativas "
                        + "não podem possuir alternativas.",
                exception.getMessage()
        );

        verifyNoInteractions(alternativaMapper);

        verify(
                alternativaRepository,
                never()
        ).save(any(Alternativa.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComOrdemDuplicada() {
        Questao questaoOriginal =
                criarQuestao(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        Questao novaQuestao =
                criarQuestao(
                        2L,
                        TipoQuestao.VERDADEIRO_FALSO
                );

        Alternativa alternativa =
                criarAlternativa(questaoOriginal);

        AlternativaUpdateRequest request =
                criarUpdateRequest();

        when(alternativaRepository.findById(10L))
                .thenReturn(Optional.of(alternativa));

        when(questaoRepository.findById(2L))
                .thenReturn(Optional.of(novaQuestao));

        when(alternativaRepository
                .existsByQuestaoIdAndOrdemAndIdNot(
                        2L,
                        3,
                        10L
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> alternativaService.atualizar(
                                10L,
                                request
                        )
                );

        assertEquals(
                "Já existe uma alternativa "
                        + "com esta ordem na questão.",
                exception.getMessage()
        );

        verify(
                alternativaMapper,
                never()
        ).updateEntity(
                any(Alternativa.class),
                any(AlternativaUpdateRequest.class),
                any(Questao.class)
        );

        verify(
                alternativaRepository,
                never()
        ).save(any(Alternativa.class));
    }

    @Test
    void deveExcluirAlternativaExistente() {
        Questao questao =
                criarQuestao(
                        1L,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        Alternativa alternativa =
                criarAlternativa(questao);

        when(alternativaRepository.findById(10L))
                .thenReturn(Optional.of(alternativa));

        alternativaService.excluir(10L);

        verify(alternativaRepository)
                .delete(alternativa);
    }

    @Test
    void deveLancarExcecaoAoExcluirAlternativaInexistente() {
        when(alternativaRepository.findById(80L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> alternativaService.excluir(80L)
                );

        assertEquals(
                "Alternativa não encontrada com o ID: 80",
                exception.getMessage()
        );

        verify(
                alternativaRepository,
                never()
        ).delete(any(Alternativa.class));
    }

    private AlternativaRequest criarRequest() {
        AlternativaRequest request =
                new AlternativaRequest();

        request.setQuestaoId(1L);
        request.setTexto("Processador");
        request.setCorreta(true);
        request.setOrdem(1);

        return request;
    }

    private AlternativaUpdateRequest criarUpdateRequest() {
        AlternativaUpdateRequest request =
                new AlternativaUpdateRequest();

        request.setQuestaoId(2L);
        request.setTexto("Memória RAM");
        request.setCorreta(false);
        request.setOrdem(3);

        return request;
    }

    private Questao criarQuestao(
            Long id,
            TipoQuestao tipo) {

        Questao questao =
                new Questao();

        questao.setId(id);
        questao.setTipo(tipo);

        questao.setEnunciado(
                "Qual componente executa as instruções?"
        );

        return questao;
    }

    private Alternativa criarAlternativa(
            Questao questao) {

        Alternativa alternativa =
                new Alternativa();

        alternativa.setId(10L);
        alternativa.setQuestao(questao);
        alternativa.setTexto("Processador");
        alternativa.setCorreta(true);
        alternativa.setOrdem(1);

        return alternativa;
    }

    private AlternativaResponse criarResponse(
            Long id,
            Long questaoId,
            String texto,
            Boolean correta,
            Integer ordem) {

        AlternativaResponse response =
                new AlternativaResponse();

        response.setId(id);
        response.setQuestaoId(questaoId);
        response.setTexto(texto);
        response.setCorreta(correta);
        response.setOrdem(ordem);

        return response;
    }
}
