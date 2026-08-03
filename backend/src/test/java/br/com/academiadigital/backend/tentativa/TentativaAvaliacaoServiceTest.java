package br.com.academiadigital.backend.tentativa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.academiadigital.backend.avaliacao.Alternativa;
import br.com.academiadigital.backend.avaliacao.AlternativaRepository;
import br.com.academiadigital.backend.avaliacao.Avaliacao;
import br.com.academiadigital.backend.avaliacao.AvaliacaoRepository;
import br.com.academiadigital.backend.avaliacao.Questao;
import br.com.academiadigital.backend.avaliacao.QuestaoRepository;
import br.com.academiadigital.backend.avaliacao.StatusAvaliacao;
import br.com.academiadigital.backend.avaliacao.TipoQuestao;
import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.matricula.Matricula;
import br.com.academiadigital.backend.matricula.MatriculaRepository;
import br.com.academiadigital.backend.matricula.StatusMatricula;
import br.com.academiadigital.backend.tentativa.dto.CorrecaoRespostaRequest;
import br.com.academiadigital.backend.tentativa.dto.RespostaQuestaoRequest;
import br.com.academiadigital.backend.tentativa.dto.RespostaQuestaoResponse;
import br.com.academiadigital.backend.tentativa.dto.TentativaAvaliacaoResponse;
import br.com.academiadigital.backend.tentativa.mapper.TentativaAvaliacaoMapper;
import br.com.academiadigital.backend.usuario.Perfil;
import br.com.academiadigital.backend.usuario.Usuario;
import br.com.academiadigital.backend.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class TentativaAvaliacaoServiceTest {

    private static final String EMAIL = "aluno@email.com";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private QuestaoRepository questaoRepository;

    @Mock
    private AlternativaRepository alternativaRepository;

    @Mock
    private TentativaAvaliacaoRepository tentativaRepository;

    @Mock
    private RespostaQuestaoRepository respostaRepository;

    @Mock
    private TentativaAvaliacaoMapper tentativaMapper;

    private TentativaAvaliacaoService tentativaService;

    @BeforeEach
    void configurar() {
        tentativaService = new TentativaAvaliacaoService(
                usuarioRepository,
                matriculaRepository,
                avaliacaoRepository,
                questaoRepository,
                alternativaRepository,
                tentativaRepository,
                respostaRepository,
                tentativaMapper
        );
    }

    @Test
    void deveIniciarTentativa() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.MULTIPLA_ESCOLHA,
                "10.00"
        );
        List<Alternativa> alternativas =
                criarAlternativas(questao);
        TentativaAvaliacaoResponse responseEsperada =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.EM_ANDAMENTO
                );

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));
        when(matriculaRepository.findByAlunoIdAndCursoId(
                1L,
                2L
        )).thenReturn(Optional.of(matricula));
        when(questaoRepository
                .findAllByAvaliacaoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(questao));
        when(alternativaRepository
                .findAllByQuestaoIdOrderByOrdemAsc(20L))
                .thenReturn(alternativas);
        when(tentativaRepository
                .findFirstByMatriculaIdAndAvaliacaoIdAndStatusOrderByNumeroTentativaDesc(
                        3L,
                        10L,
                        StatusTentativa.EM_ANDAMENTO
                )).thenReturn(Optional.empty());
        when(tentativaRepository
                .countByMatriculaIdAndAvaliacaoId(3L, 10L))
                .thenReturn(1L);
        when(tentativaRepository.save(
                any(TentativaAvaliacao.class)
        )).thenAnswer(invocation -> {
            TentativaAvaliacao tentativa = invocation.getArgument(0);
            tentativa.setId(100L);
            return tentativa;
        });
        when(tentativaMapper.toResponse(
                any(TentativaAvaliacao.class)
        )).thenReturn(responseEsperada);

        TentativaAvaliacaoResponse response =
                tentativaService.iniciar(EMAIL, 10L);

        assertEquals(100L, response.getId());
        assertEquals(
                StatusTentativa.EM_ANDAMENTO,
                response.getStatus()
        );

        verify(tentativaRepository).save(
                any(TentativaAvaliacao.class)
        );
    }

    @Test
    void deveRejeitarAvaliacaoNaoPublicada() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        avaliacao.setStatus(StatusAvaliacao.RASCUNHO);

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tentativaService.iniciar(EMAIL, 10L)
        );

        assertEquals(
                "Somente avaliações publicadas podem ser iniciadas.",
                exception.getMessage()
        );
        verify(tentativaRepository, never()).save(any());
    }

    @Test
    void deveRejeitarAlunoSemMatricula() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));
        when(matriculaRepository.findByAlunoIdAndCursoId(
                1L,
                2L
        )).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> tentativaService.iniciar(EMAIL, 10L)
        );

        assertEquals(
                "Matrícula não encontrada para o aluno autenticado neste curso.",
                exception.getMessage()
        );
    }

    @Test
    void deveRejeitarQuandoExisteTentativaEmAndamento() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.MULTIPLA_ESCOLHA,
                "10.00"
        );
        TentativaAvaliacao tentativa =
                criarTentativa(matricula, avaliacao);

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));
        when(matriculaRepository.findByAlunoIdAndCursoId(
                1L,
                2L
        )).thenReturn(Optional.of(matricula));
        when(questaoRepository
                .findAllByAvaliacaoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(questao));
        when(alternativaRepository
                .findAllByQuestaoIdOrderByOrdemAsc(20L))
                .thenReturn(criarAlternativas(questao));
        when(tentativaRepository
                .findFirstByMatriculaIdAndAvaliacaoIdAndStatusOrderByNumeroTentativaDesc(
                        3L,
                        10L,
                        StatusTentativa.EM_ANDAMENTO
                )).thenReturn(Optional.of(tentativa));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tentativaService.iniciar(EMAIL, 10L)
        );

        assertEquals(
                "Já existe uma tentativa em andamento para esta avaliação.",
                exception.getMessage()
        );
    }

    @Test
    void deveRejeitarQuandoMaximoDeTentativasFoiAtingido() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.MULTIPLA_ESCOLHA,
                "10.00"
        );

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(avaliacaoRepository.findById(10L))
                .thenReturn(Optional.of(avaliacao));
        when(matriculaRepository.findByAlunoIdAndCursoId(
                1L,
                2L
        )).thenReturn(Optional.of(matricula));
        when(questaoRepository
                .findAllByAvaliacaoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(questao));
        when(alternativaRepository
                .findAllByQuestaoIdOrderByOrdemAsc(20L))
                .thenReturn(criarAlternativas(questao));
        when(tentativaRepository
                .findFirstByMatriculaIdAndAvaliacaoIdAndStatusOrderByNumeroTentativaDesc(
                        3L,
                        10L,
                        StatusTentativa.EM_ANDAMENTO
                )).thenReturn(Optional.empty());
        when(tentativaRepository
                .countByMatriculaIdAndAvaliacaoId(3L, 10L))
                .thenReturn(3L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tentativaService.iniciar(EMAIL, 10L)
        );

        assertEquals(
                "O número máximo de tentativas desta avaliação foi atingido.",
                exception.getMessage()
        );
    }

    @Test
    void deveSalvarRespostaObjetiva() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        TentativaAvaliacao tentativa =
                criarTentativa(matricula, avaliacao);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.MULTIPLA_ESCOLHA,
                "10.00"
        );
        Alternativa alternativa =
                criarAlternativas(questao).get(0);
        RespostaQuestaoRequest request =
                new RespostaQuestaoRequest();
        request.setAlternativaId(30L);
        RespostaQuestaoResponse responseEsperada =
                new RespostaQuestaoResponse();
        responseEsperada.setId(40L);
        responseEsperada.setQuestaoId(20L);
        responseEsperada.setAlternativaSelecionadaId(30L);

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(tentativaRepository
                .findByIdAndMatriculaAlunoId(100L, 1L))
                .thenReturn(Optional.of(tentativa));
        when(questaoRepository.findById(20L))
                .thenReturn(Optional.of(questao));
        when(respostaRepository
                .findByTentativaIdAndQuestaoId(100L, 20L))
                .thenReturn(Optional.empty());
        when(alternativaRepository.findById(30L))
                .thenReturn(Optional.of(alternativa));
        when(respostaRepository.save(
                any(RespostaQuestao.class)
        )).thenAnswer(invocation -> {
            RespostaQuestao resposta = invocation.getArgument(0);
            resposta.setId(40L);
            return resposta;
        });
        when(tentativaMapper.toRespostaResponse(
                any(RespostaQuestao.class)
        )).thenReturn(responseEsperada);

        RespostaQuestaoResponse response =
                tentativaService.salvarResposta(
                        EMAIL,
                        100L,
                        20L,
                        request
                );

        assertEquals(40L, response.getId());
        assertEquals(30L, response.getAlternativaSelecionadaId());
    }

    @Test
    void deveRejeitarAlternativaDeOutraQuestao() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        TentativaAvaliacao tentativa =
                criarTentativa(matricula, avaliacao);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.MULTIPLA_ESCOLHA,
                "10.00"
        );
        Questao outraQuestao = criarQuestao(
                avaliacao,
                21L,
                TipoQuestao.MULTIPLA_ESCOLHA,
                "10.00"
        );
        Alternativa alternativa = new Alternativa();
        alternativa.setId(30L);
        alternativa.setQuestao(outraQuestao);
        RespostaQuestaoRequest request =
                new RespostaQuestaoRequest();
        request.setAlternativaId(30L);

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(tentativaRepository
                .findByIdAndMatriculaAlunoId(100L, 1L))
                .thenReturn(Optional.of(tentativa));
        when(questaoRepository.findById(20L))
                .thenReturn(Optional.of(questao));
        when(respostaRepository
                .findByTentativaIdAndQuestaoId(100L, 20L))
                .thenReturn(Optional.empty());
        when(alternativaRepository.findById(30L))
                .thenReturn(Optional.of(alternativa));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tentativaService.salvarResposta(
                        EMAIL,
                        100L,
                        20L,
                        request
                )
        );

        assertEquals(
                "A alternativa selecionada não pertence à questão informada.",
                exception.getMessage()
        );
    }

    @Test
    void deveSalvarRespostaDissertativa() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        TentativaAvaliacao tentativa =
                criarTentativa(matricula, avaliacao);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.DISSERTATIVA,
                "10.00"
        );
        RespostaQuestaoRequest request =
                new RespostaQuestaoRequest();
        request.setRespostaTexto("  Resposta do aluno.  ");
        RespostaQuestaoResponse responseEsperada =
                new RespostaQuestaoResponse();
        responseEsperada.setId(40L);
        responseEsperada.setRespostaTexto(
                "Resposta do aluno."
        );

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(tentativaRepository
                .findByIdAndMatriculaAlunoId(100L, 1L))
                .thenReturn(Optional.of(tentativa));
        when(questaoRepository.findById(20L))
                .thenReturn(Optional.of(questao));
        when(respostaRepository
                .findByTentativaIdAndQuestaoId(100L, 20L))
                .thenReturn(Optional.empty());
        when(respostaRepository.save(
                any(RespostaQuestao.class)
        )).thenAnswer(invocation -> {
            RespostaQuestao resposta = invocation.getArgument(0);
            resposta.setId(40L);
            return resposta;
        });
        when(tentativaMapper.toRespostaResponse(
                any(RespostaQuestao.class)
        )).thenReturn(responseEsperada);

        RespostaQuestaoResponse response =
                tentativaService.salvarResposta(
                        EMAIL,
                        100L,
                        20L,
                        request
                );

        assertEquals(
                "Resposta do aluno.",
                response.getRespostaTexto()
        );
    }

    @Test
    void deveFinalizarTentativaObjetivaComAprovacao() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        TentativaAvaliacao tentativa =
                criarTentativa(matricula, avaliacao);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.MULTIPLA_ESCOLHA,
                "10.00"
        );
        Alternativa alternativaCorreta =
                criarAlternativas(questao).get(0);
        RespostaQuestao resposta = criarResposta(
                tentativa,
                questao,
                alternativaCorreta
        );
        TentativaAvaliacaoResponse responseEsperada =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.FINALIZADA
                );

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(tentativaRepository
                .findByIdAndMatriculaAlunoId(100L, 1L))
                .thenReturn(Optional.of(tentativa));
        when(questaoRepository
                .findAllByAvaliacaoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(questao));
        when(respostaRepository
                .findAllByTentativaIdOrderByQuestaoOrdemAsc(100L))
                .thenReturn(List.of(resposta));
        when(tentativaRepository.save(tentativa))
                .thenReturn(tentativa);
        when(tentativaMapper.toResponse(tentativa))
                .thenReturn(responseEsperada);

        TentativaAvaliacaoResponse response =
                tentativaService.finalizar(EMAIL, 100L);

        assertEquals(
                StatusTentativa.FINALIZADA,
                tentativa.getStatus()
        );
        assertEquals(
                new BigDecimal("10.00"),
                tentativa.getNota()
        );
        assertTrue(tentativa.getAprovado());
        assertTrue(resposta.getCorrigida());
        assertTrue(resposta.getCorreta());
        assertEquals(
                StatusTentativa.FINALIZADA,
                response.getStatus()
        );
    }

    @Test
    void deveAguardarCorrecaoQuandoPossuiQuestaoDissertativa() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        TentativaAvaliacao tentativa =
                criarTentativa(matricula, avaliacao);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.DISSERTATIVA,
                "10.00"
        );
        RespostaQuestao resposta = new RespostaQuestao();
        resposta.setId(40L);
        resposta.setTentativa(tentativa);
        resposta.setQuestao(questao);
        resposta.setRespostaTexto("Resposta.");
        resposta.setPontuacaoObtida(BigDecimal.ZERO);
        TentativaAvaliacaoResponse responseEsperada =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.AGUARDANDO_CORRECAO
                );

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(tentativaRepository
                .findByIdAndMatriculaAlunoId(100L, 1L))
                .thenReturn(Optional.of(tentativa));
        when(questaoRepository
                .findAllByAvaliacaoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(questao));
        when(respostaRepository
                .findAllByTentativaIdOrderByQuestaoOrdemAsc(100L))
                .thenReturn(List.of(resposta));
        when(tentativaRepository.save(tentativa))
                .thenReturn(tentativa);
        when(tentativaMapper.toResponse(tentativa))
                .thenReturn(responseEsperada);

        TentativaAvaliacaoResponse response =
                tentativaService.finalizar(EMAIL, 100L);

        assertEquals(
                StatusTentativa.AGUARDANDO_CORRECAO,
                tentativa.getStatus()
        );
        assertNull(tentativa.getNota());
        assertNull(tentativa.getAprovado());
        assertFalse(resposta.getCorrigida());
        assertEquals(
                StatusTentativa.AGUARDANDO_CORRECAO,
                response.getStatus()
        );
    }

    @Test
    void deveRejeitarFinalizacaoComQuestaoSemResposta() {
        Usuario aluno = criarAluno();
        Avaliacao avaliacao = criarAvaliacao();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        TentativaAvaliacao tentativa =
                criarTentativa(matricula, avaliacao);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.MULTIPLA_ESCOLHA,
                "10.00"
        );

        when(usuarioRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(aluno));
        when(tentativaRepository
                .findByIdAndMatriculaAlunoId(100L, 1L))
                .thenReturn(Optional.of(tentativa));
        when(questaoRepository
                .findAllByAvaliacaoIdOrderByOrdemAsc(10L))
                .thenReturn(List.of(questao));
        when(respostaRepository
                .findAllByTentativaIdOrderByQuestaoOrdemAsc(100L))
                .thenReturn(List.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tentativaService.finalizar(EMAIL, 100L)
        );

        assertEquals(
                "Todas as questões devem ser respondidas antes da finalização.",
                exception.getMessage()
        );
    }

    @Test
    void deveCorrigirRespostaDissertativaEFinalizarTentativa() {
        Avaliacao avaliacao = criarAvaliacao();
        Usuario aluno = criarAluno();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        TentativaAvaliacao tentativa =
                criarTentativa(matricula, avaliacao);
        tentativa.setStatus(
                StatusTentativa.AGUARDANDO_CORRECAO
        );
        tentativa.setPontuacaoTotal(
                new BigDecimal("10.00")
        );
        tentativa.setPontuacaoObtida(BigDecimal.ZERO);
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.DISSERTATIVA,
                "10.00"
        );
        RespostaQuestao resposta = new RespostaQuestao();
        resposta.setId(40L);
        resposta.setTentativa(tentativa);
        resposta.setQuestao(questao);
        resposta.setRespostaTexto("Resposta.");
        resposta.setCorrigida(false);
        resposta.setPontuacaoObtida(BigDecimal.ZERO);
        CorrecaoRespostaRequest request =
                new CorrecaoRespostaRequest();
        request.setPontuacaoObtida(
                new BigDecimal("8.00")
        );
        request.setFeedback("  Boa resposta.  ");
        TentativaAvaliacaoResponse responseEsperada =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.FINALIZADA
                );

        when(tentativaRepository.findById(100L))
                .thenReturn(Optional.of(tentativa));
        when(respostaRepository
                .findByTentativaIdAndQuestaoId(100L, 20L))
                .thenReturn(Optional.of(resposta));
        when(respostaRepository
                .findAllByTentativaIdOrderByQuestaoOrdemAsc(100L))
                .thenReturn(List.of(resposta));
        when(tentativaMapper.toResponse(tentativa))
                .thenReturn(responseEsperada);

        TentativaAvaliacaoResponse response =
                tentativaService.corrigirResposta(
                        100L,
                        20L,
                        request
                );

        assertTrue(resposta.getCorrigida());
        assertEquals(
                new BigDecimal("8.00"),
                resposta.getPontuacaoObtida()
        );
        assertEquals("Boa resposta.", resposta.getFeedback());
        assertEquals(
                StatusTentativa.FINALIZADA,
                tentativa.getStatus()
        );
        assertEquals(
                new BigDecimal("8.00"),
                tentativa.getNota()
        );
        assertTrue(tentativa.getAprovado());
        assertEquals(
                StatusTentativa.FINALIZADA,
                response.getStatus()
        );
    }

    @Test
    void deveRejeitarPontuacaoManualMaiorQuePontuacaoDaQuestao() {
        Avaliacao avaliacao = criarAvaliacao();
        Usuario aluno = criarAluno();
        Matricula matricula = criarMatricula(aluno, avaliacao);
        TentativaAvaliacao tentativa =
                criarTentativa(matricula, avaliacao);
        tentativa.setStatus(
                StatusTentativa.AGUARDANDO_CORRECAO
        );
        Questao questao = criarQuestao(
                avaliacao,
                20L,
                TipoQuestao.DISSERTATIVA,
                "5.00"
        );
        RespostaQuestao resposta = new RespostaQuestao();
        resposta.setTentativa(tentativa);
        resposta.setQuestao(questao);
        CorrecaoRespostaRequest request =
                new CorrecaoRespostaRequest();
        request.setPontuacaoObtida(
                new BigDecimal("6.00")
        );

        when(tentativaRepository.findById(100L))
                .thenReturn(Optional.of(tentativa));
        when(respostaRepository
                .findByTentativaIdAndQuestaoId(100L, 20L))
                .thenReturn(Optional.of(resposta));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> tentativaService.corrigirResposta(
                        100L,
                        20L,
                        request
                )
        );

        assertEquals(
                "A pontuação obtida não pode ser maior que a pontuação da questão.",
                exception.getMessage()
        );
    }

    private Usuario criarAluno() {
        Usuario aluno = new Usuario();
        aluno.setId(1L);
        aluno.setNome("Aluno Teste");
        aluno.setEmail(EMAIL);
        aluno.setPerfil(Perfil.ALUNO);
        aluno.setAtivo(true);
        return aluno;
    }

    private Avaliacao criarAvaliacao() {
        Curso curso = new Curso();
        curso.setId(2L);
        curso.setTitulo("Informática Básica");

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setId(10L);
        avaliacao.setCurso(curso);
        avaliacao.setTitulo("Avaliação final");
        avaliacao.setNotaMinima(new BigDecimal("7.00"));
        avaliacao.setMaximoTentativas(3);
        avaliacao.setStatus(StatusAvaliacao.PUBLICADA);
        return avaliacao;
    }

    private Matricula criarMatricula(
            Usuario aluno,
            Avaliacao avaliacao) {

        Matricula matricula = new Matricula();
        matricula.setId(3L);
        matricula.setAluno(aluno);
        matricula.setCurso(avaliacao.getCurso());
        matricula.setStatus(StatusMatricula.ATIVA);
        return matricula;
    }

    private TentativaAvaliacao criarTentativa(
            Matricula matricula,
            Avaliacao avaliacao) {

        TentativaAvaliacao tentativa =
                new TentativaAvaliacao();
        tentativa.setId(100L);
        tentativa.setMatricula(matricula);
        tentativa.setAvaliacao(avaliacao);
        tentativa.setNumeroTentativa(1);
        tentativa.setStatus(StatusTentativa.EM_ANDAMENTO);
        return tentativa;
    }

    private Questao criarQuestao(
            Avaliacao avaliacao,
            Long id,
            TipoQuestao tipo,
            String pontuacao) {

        Questao questao = new Questao();
        questao.setId(id);
        questao.setAvaliacao(avaliacao);
        questao.setEnunciado("Enunciado");
        questao.setTipo(tipo);
        questao.setOrdem(1);
        questao.setPontuacao(new BigDecimal(pontuacao));
        return questao;
    }

    private List<Alternativa> criarAlternativas(
            Questao questao) {

        Alternativa correta = new Alternativa();
        correta.setId(30L);
        correta.setQuestao(questao);
        correta.setTexto("Correta");
        correta.setCorreta(true);
        correta.setOrdem(1);

        Alternativa incorreta = new Alternativa();
        incorreta.setId(31L);
        incorreta.setQuestao(questao);
        incorreta.setTexto("Incorreta");
        incorreta.setCorreta(false);
        incorreta.setOrdem(2);

        return List.of(correta, incorreta);
    }

    private RespostaQuestao criarResposta(
            TentativaAvaliacao tentativa,
            Questao questao,
            Alternativa alternativa) {

        RespostaQuestao resposta = new RespostaQuestao();
        resposta.setId(40L);
        resposta.setTentativa(tentativa);
        resposta.setQuestao(questao);
        resposta.setAlternativaSelecionada(alternativa);
        resposta.setCorrigida(false);
        resposta.setPontuacaoObtida(BigDecimal.ZERO);
        return resposta;
    }

    private TentativaAvaliacaoResponse criarTentativaResponse(
            Long id,
            StatusTentativa status) {

        TentativaAvaliacaoResponse response =
                new TentativaAvaliacaoResponse();
        response.setId(id);
        response.setStatus(status);
        return response;
    }
}
