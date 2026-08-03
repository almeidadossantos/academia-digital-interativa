package br.com.academiadigital.backend.tentativa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.avaliacao.Alternativa;
import br.com.academiadigital.backend.avaliacao.AlternativaRepository;
import br.com.academiadigital.backend.avaliacao.Avaliacao;
import br.com.academiadigital.backend.avaliacao.AvaliacaoRepository;
import br.com.academiadigital.backend.avaliacao.Questao;
import br.com.academiadigital.backend.avaliacao.QuestaoRepository;
import br.com.academiadigital.backend.avaliacao.StatusAvaliacao;
import br.com.academiadigital.backend.avaliacao.TipoQuestao;
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

@Service
public class TentativaAvaliacaoService {

    private static final BigDecimal NOTA_MAXIMA =
            new BigDecimal("10.00");

    private final UsuarioRepository usuarioRepository;
    private final MatriculaRepository matriculaRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaRepository alternativaRepository;
    private final TentativaAvaliacaoRepository tentativaRepository;
    private final RespostaQuestaoRepository respostaRepository;
    private final TentativaAvaliacaoMapper tentativaMapper;

    public TentativaAvaliacaoService(
            UsuarioRepository usuarioRepository,
            MatriculaRepository matriculaRepository,
            AvaliacaoRepository avaliacaoRepository,
            QuestaoRepository questaoRepository,
            AlternativaRepository alternativaRepository,
            TentativaAvaliacaoRepository tentativaRepository,
            RespostaQuestaoRepository respostaRepository,
            TentativaAvaliacaoMapper tentativaMapper) {

        this.usuarioRepository = usuarioRepository;
        this.matriculaRepository = matriculaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.questaoRepository = questaoRepository;
        this.alternativaRepository = alternativaRepository;
        this.tentativaRepository = tentativaRepository;
        this.respostaRepository = respostaRepository;
        this.tentativaMapper = tentativaMapper;
    }

    @Transactional
    public TentativaAvaliacaoResponse iniciar(
            String email,
            Long avaliacaoId) {

        Usuario aluno = buscarAlunoAutenticado(email);
        Avaliacao avaliacao = buscarAvaliacao(avaliacaoId);

        validarAvaliacaoDisponivel(avaliacao);

        Matricula matricula = buscarMatriculaAtiva(
                aluno.getId(),
                avaliacao.getCurso().getId()
        );

        validarConfiguracaoDaAvaliacao(avaliacaoId);
        finalizarTentativaExpiradaSeExistir(
                matricula.getId(),
                avaliacaoId
        );
        validarTentativaAguardandoCorrecao(
                matricula.getId(),
                avaliacaoId
        );

        long quantidadeTentativas = tentativaRepository
                .countByMatriculaIdAndAvaliacaoId(
                        matricula.getId(),
                        avaliacaoId
                );

        if (quantidadeTentativas >= avaliacao.getMaximoTentativas()) {
            throw new IllegalArgumentException(
                    "O número máximo de tentativas desta avaliação foi atingido."
            );
        }

        TentativaAvaliacao tentativa = new TentativaAvaliacao();
        LocalDateTime agora = LocalDateTime.now();

        tentativa.setMatricula(matricula);
        tentativa.setAvaliacao(avaliacao);
        tentativa.setNumeroTentativa(
                Math.toIntExact(quantidadeTentativas + 1)
        );
        tentativa.setStatus(StatusTentativa.EM_ANDAMENTO);
        tentativa.setDataInicio(agora);

        if (avaliacao.getTempoLimiteMinutos() != null) {
            tentativa.setDataLimite(
                    agora.plusMinutes(
                            avaliacao.getTempoLimiteMinutos()
                    )
            );
        }

        TentativaAvaliacao tentativaSalva =
                tentativaRepository.save(tentativa);

        return tentativaMapper.toResponse(tentativaSalva);
    }

    @Transactional
    public RespostaQuestaoResponse salvarResposta(
            String email,
            Long tentativaId,
            Long questaoId,
            RespostaQuestaoRequest request) {

        Usuario aluno = buscarAlunoAutenticado(email);
        TentativaAvaliacao tentativa =
                buscarTentativaDoAluno(tentativaId, aluno.getId());

        validarTentativaEmAndamento(tentativa);

        Questao questao = buscarQuestao(questaoId);
        validarQuestaoDaTentativa(tentativa, questao);

        RespostaQuestao resposta = respostaRepository
                .findByTentativaIdAndQuestaoId(
                        tentativaId,
                        questaoId
                )
                .orElseGet(() -> criarResposta(
                        tentativa,
                        questao
                ));

        preencherResposta(resposta, questao, request);

        RespostaQuestao respostaSalva =
                respostaRepository.save(resposta);

        return tentativaMapper.toRespostaResponse(respostaSalva);
    }

    @Transactional
    public TentativaAvaliacaoResponse finalizar(
            String email,
            Long tentativaId) {

        Usuario aluno = buscarAlunoAutenticado(email);
        TentativaAvaliacao tentativa =
                buscarTentativaDoAluno(tentativaId, aluno.getId());

        validarTentativaEmAndamento(tentativa);

        List<Questao> questoes = questaoRepository
                .findAllByAvaliacaoIdOrderByOrdemAsc(
                        tentativa.getAvaliacao().getId()
                );

        List<RespostaQuestao> respostas = respostaRepository
                .findAllByTentativaIdOrderByQuestaoOrdemAsc(
                        tentativaId
                );

        validarTodasQuestoesRespondidas(questoes, respostas);

        boolean possuiQuestaoDissertativa = false;

        for (RespostaQuestao resposta : respostas) {
            if (resposta.getQuestao().getTipo()
                    == TipoQuestao.DISSERTATIVA) {

                resposta.setCorrigida(false);
                resposta.setCorreta(null);
                resposta.setPontuacaoObtida(BigDecimal.ZERO);
                possuiQuestaoDissertativa = true;
            } else {
                corrigirRespostaObjetiva(resposta);
            }
        }

        respostaRepository.saveAll(respostas);

        LocalDateTime agora = LocalDateTime.now();
        tentativa.setDataEnvio(agora);
        tentativa.setPontuacaoTotal(
                calcularPontuacaoTotal(questoes)
        );
        tentativa.setPontuacaoObtida(
                calcularPontuacaoObtida(respostas)
        );

        if (possuiQuestaoDissertativa) {
            tentativa.setStatus(
                    StatusTentativa.AGUARDANDO_CORRECAO
            );
            tentativa.setNota(null);
            tentativa.setAprovado(null);
        } else {
            concluirTentativa(tentativa, agora);
        }

        TentativaAvaliacao tentativaSalva =
                tentativaRepository.save(tentativa);

        return tentativaMapper.toResponse(tentativaSalva);
    }

    @Transactional
    public TentativaAvaliacaoResponse corrigirResposta(
            Long tentativaId,
            Long questaoId,
            CorrecaoRespostaRequest request) {

        TentativaAvaliacao tentativa =
                buscarTentativa(tentativaId);

        if (tentativa.getStatus()
                != StatusTentativa.AGUARDANDO_CORRECAO) {

            throw new IllegalArgumentException(
                    "A tentativa não está aguardando correção."
            );
        }

        RespostaQuestao resposta = respostaRepository
                .findByTentativaIdAndQuestaoId(
                        tentativaId,
                        questaoId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resposta não encontrada para a questão de ID: "
                                        + questaoId
                        )
                );

        if (resposta.getQuestao().getTipo()
                != TipoQuestao.DISSERTATIVA) {

            throw new IllegalArgumentException(
                    "Somente respostas dissertativas exigem correção manual."
            );
        }

        if (request.getPontuacaoObtida().compareTo(
                resposta.getQuestao().getPontuacao()
        ) > 0) {
            throw new IllegalArgumentException(
                    "A pontuação obtida não pode ser maior que a pontuação da questão."
            );
        }

        resposta.setPontuacaoObtida(
                request.getPontuacaoObtida()
        );
        resposta.setFeedback(normalizarTexto(request.getFeedback()));
        resposta.setCorrigida(true);
        resposta.setCorreta(null);

        respostaRepository.save(resposta);

        finalizarAposUltimaCorrecao(tentativa);

        return tentativaMapper.toResponse(tentativa);
    }

    @Transactional(readOnly = true)
    public Page<TentativaAvaliacaoResponse> listarMinhas(
            String email,
            Pageable pageable) {

        Usuario aluno = buscarAlunoAutenticado(email);

        return tentativaRepository
                .findAllByMatriculaAlunoId(
                        aluno.getId(),
                        pageable
                )
                .map(tentativaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TentativaAvaliacaoResponse> listarTodos(
            Long avaliacaoId,
            Pageable pageable) {

        Page<TentativaAvaliacao> pagina;

        if (avaliacaoId == null) {
            pagina = tentativaRepository.findAll(pageable);
        } else {
            pagina = tentativaRepository.findAllByAvaliacaoId(
                    avaliacaoId,
                    pageable
            );
        }

        return pagina.map(tentativaMapper::toResponse);
    }

    @Transactional
    public TentativaAvaliacaoResponse buscarPorId(
            String email,
            Long tentativaId) {

        Usuario usuario = buscarUsuarioAutenticado(email);
        TentativaAvaliacao tentativa =
                buscarTentativa(tentativaId);

        if (usuario.getPerfil() == Perfil.ALUNO
                && !tentativa.getMatricula()
                        .getAluno()
                        .getId()
                        .equals(usuario.getId())) {

            throw new IllegalArgumentException(
                    "O aluno autenticado não possui acesso a esta tentativa."
            );
        }

        atualizarExpiracaoSeNecessario(tentativa);

        return tentativaMapper.toResponse(tentativa);
    }

    private Usuario buscarAlunoAutenticado(String email) {
        Usuario usuario = buscarUsuarioAutenticado(email);

        if (usuario.getPerfil() != Perfil.ALUNO) {
            throw new IllegalArgumentException(
                    "O usuário autenticado não possui o perfil ALUNO."
            );
        }

        return usuario;
    }

    private Usuario buscarUsuarioAutenticado(String email) {
        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário autenticado não encontrado."
                        )
                );

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new IllegalArgumentException(
                    "O usuário autenticado está inativo."
            );
        }

        return usuario;
    }

    private Avaliacao buscarAvaliacao(Long avaliacaoId) {
        return avaliacaoRepository.findById(avaliacaoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Avaliação não encontrada com o ID: "
                                        + avaliacaoId
                        )
                );
    }

    private Questao buscarQuestao(Long questaoId) {
        return questaoRepository.findById(questaoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Questão não encontrada com o ID: "
                                        + questaoId
                        )
                );
    }

    private TentativaAvaliacao buscarTentativa(
            Long tentativaId) {

        return tentativaRepository.findById(tentativaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tentativa não encontrada com o ID: "
                                        + tentativaId
                        )
                );
    }

    private TentativaAvaliacao buscarTentativaDoAluno(
            Long tentativaId,
            Long alunoId) {

        return tentativaRepository
                .findByIdAndMatriculaAlunoId(
                        tentativaId,
                        alunoId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tentativa não encontrada para o aluno autenticado."
                        )
                );
    }

    private Matricula buscarMatriculaAtiva(
            Long alunoId,
            Long cursoId) {

        Matricula matricula = matriculaRepository
                .findByAlunoIdAndCursoId(
                        alunoId,
                        cursoId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Matrícula não encontrada para o aluno autenticado neste curso."
                        )
                );

        if (matricula.getStatus() != StatusMatricula.ATIVA) {
            throw new IllegalArgumentException(
                    "A matrícula do aluno neste curso não está ativa."
            );
        }

        return matricula;
    }

    private void validarAvaliacaoDisponivel(
            Avaliacao avaliacao) {

        if (avaliacao.getStatus() != StatusAvaliacao.PUBLICADA) {
            throw new IllegalArgumentException(
                    "Somente avaliações publicadas podem ser iniciadas."
            );
        }
    }

    private void validarConfiguracaoDaAvaliacao(
            Long avaliacaoId) {

        List<Questao> questoes = questaoRepository
                .findAllByAvaliacaoIdOrderByOrdemAsc(
                        avaliacaoId
                );

        if (questoes.isEmpty()) {
            throw new IllegalArgumentException(
                    "A avaliação não possui questões cadastradas."
            );
        }

        for (Questao questao : questoes) {
            if (questao.getTipo() == TipoQuestao.DISSERTATIVA) {
                continue;
            }

            List<Alternativa> alternativas = alternativaRepository
                    .findAllByQuestaoIdOrderByOrdemAsc(
                            questao.getId()
                    );

            if (questao.getTipo() == TipoQuestao.VERDADEIRO_FALSO
                    && alternativas.size() != 2) {

                throw new IllegalArgumentException(
                        "A questão de verdadeiro ou falso de ID "
                                + questao.getId()
                                + " deve possuir exatamente duas alternativas."
                );
            }

            if (questao.getTipo() == TipoQuestao.MULTIPLA_ESCOLHA
                    && alternativas.size() < 2) {

                throw new IllegalArgumentException(
                        "A questão de ID "
                                + questao.getId()
                                + " deve possuir pelo menos duas alternativas."
                );
            }

            long alternativasCorretas = alternativas.stream()
                    .filter(alternativa ->
                            Boolean.TRUE.equals(
                                    alternativa.getCorreta()
                            )
                    )
                    .count();

            if (alternativasCorretas != 1) {
                throw new IllegalArgumentException(
                        "A questão de ID "
                                + questao.getId()
                                + " deve possuir exatamente uma alternativa correta."
                );
            }
        }
    }

    private void finalizarTentativaExpiradaSeExistir(
            Long matriculaId,
            Long avaliacaoId) {

        tentativaRepository
                .findFirstByMatriculaIdAndAvaliacaoIdAndStatusOrderByNumeroTentativaDesc(
                        matriculaId,
                        avaliacaoId,
                        StatusTentativa.EM_ANDAMENTO
                )
                .ifPresent(tentativa -> {
                    if (estaExpirada(tentativa)) {
                        expirarTentativa(tentativa);
                    } else {
                        throw new IllegalArgumentException(
                                "Já existe uma tentativa em andamento para esta avaliação."
                        );
                    }
                });
    }

    private void validarTentativaAguardandoCorrecao(
            Long matriculaId,
            Long avaliacaoId) {

        boolean aguardandoCorrecao = tentativaRepository
                .existsByMatriculaIdAndAvaliacaoIdAndStatus(
                        matriculaId,
                        avaliacaoId,
                        StatusTentativa.AGUARDANDO_CORRECAO
                );

        if (aguardandoCorrecao) {
            throw new IllegalArgumentException(
                    "Existe uma tentativa aguardando correção para esta avaliação."
            );
        }
    }

    private void validarTentativaEmAndamento(
            TentativaAvaliacao tentativa) {

        atualizarExpiracaoSeNecessario(tentativa);

        if (tentativa.getStatus()
                != StatusTentativa.EM_ANDAMENTO) {

            throw new IllegalArgumentException(
                    "A tentativa não está em andamento."
            );
        }
    }

    private void atualizarExpiracaoSeNecessario(
            TentativaAvaliacao tentativa) {

        if (tentativa.getStatus() == StatusTentativa.EM_ANDAMENTO
                && estaExpirada(tentativa)) {

            expirarTentativa(tentativa);
        }
    }

    private boolean estaExpirada(
            TentativaAvaliacao tentativa) {

        return tentativa.getDataLimite() != null
                && LocalDateTime.now().isAfter(
                        tentativa.getDataLimite()
                );
    }

    private void expirarTentativa(
            TentativaAvaliacao tentativa) {

        tentativa.setStatus(StatusTentativa.EXPIRADA);
        tentativa.setDataFinalizacao(LocalDateTime.now());
        tentativaRepository.save(tentativa);
    }

    private void validarQuestaoDaTentativa(
            TentativaAvaliacao tentativa,
            Questao questao) {

        if (!questao.getAvaliacao().getId().equals(
                tentativa.getAvaliacao().getId()
        )) {
            throw new IllegalArgumentException(
                    "A questão informada não pertence à avaliação desta tentativa."
            );
        }
    }

    private RespostaQuestao criarResposta(
            TentativaAvaliacao tentativa,
            Questao questao) {

        RespostaQuestao resposta = new RespostaQuestao();
        resposta.setTentativa(tentativa);
        resposta.setQuestao(questao);
        resposta.setCorrigida(false);
        resposta.setPontuacaoObtida(BigDecimal.ZERO);

        return resposta;
    }

    private void preencherResposta(
            RespostaQuestao resposta,
            Questao questao,
            RespostaQuestaoRequest request) {

        if (questao.getTipo() == TipoQuestao.DISSERTATIVA) {
            preencherRespostaDissertativa(resposta, request);
        } else {
            preencherRespostaObjetiva(
                    resposta,
                    questao,
                    request
            );
        }

        resposta.setCorrigida(false);
        resposta.setCorreta(null);
        resposta.setPontuacaoObtida(BigDecimal.ZERO);
        resposta.setFeedback(null);
    }

    private void preencherRespostaDissertativa(
            RespostaQuestao resposta,
            RespostaQuestaoRequest request) {

        String respostaTexto =
                normalizarTexto(request.getRespostaTexto());

        if (respostaTexto == null) {
            throw new IllegalArgumentException(
                    "A resposta textual é obrigatória para questões dissertativas."
            );
        }

        if (request.getAlternativaId() != null) {
            throw new IllegalArgumentException(
                    "Questões dissertativas não aceitam alternativa selecionada."
            );
        }

        resposta.setAlternativaSelecionada(null);
        resposta.setRespostaTexto(respostaTexto);
    }

    private void preencherRespostaObjetiva(
            RespostaQuestao resposta,
            Questao questao,
            RespostaQuestaoRequest request) {

        if (request.getAlternativaId() == null) {
            throw new IllegalArgumentException(
                    "A alternativa selecionada é obrigatória para questões objetivas."
            );
        }

        if (normalizarTexto(request.getRespostaTexto()) != null) {
            throw new IllegalArgumentException(
                    "Questões objetivas não aceitam resposta textual."
            );
        }

        Alternativa alternativa = alternativaRepository
                .findById(request.getAlternativaId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Alternativa não encontrada com o ID: "
                                        + request.getAlternativaId()
                        )
                );

        if (!alternativa.getQuestao().getId().equals(
                questao.getId()
        )) {
            throw new IllegalArgumentException(
                    "A alternativa selecionada não pertence à questão informada."
            );
        }

        resposta.setAlternativaSelecionada(alternativa);
        resposta.setRespostaTexto(null);
    }

    private void validarTodasQuestoesRespondidas(
            List<Questao> questoes,
            List<RespostaQuestao> respostas) {

        if (respostas.size() != questoes.size()) {
            throw new IllegalArgumentException(
                    "Todas as questões devem ser respondidas antes da finalização."
            );
        }

        boolean existeQuestaoSemResposta = questoes.stream()
                .anyMatch(questao ->
                        respostas.stream().noneMatch(resposta ->
                                resposta.getQuestao()
                                        .getId()
                                        .equals(questao.getId())
                        )
                );

        if (existeQuestaoSemResposta) {
            throw new IllegalArgumentException(
                    "Todas as questões devem ser respondidas antes da finalização."
            );
        }
    }

    private void corrigirRespostaObjetiva(
            RespostaQuestao resposta) {

        Alternativa alternativa =
                resposta.getAlternativaSelecionada();

        if (alternativa == null) {
            throw new IllegalArgumentException(
                    "Existe uma questão objetiva sem alternativa selecionada."
            );
        }

        boolean correta = Boolean.TRUE.equals(
                alternativa.getCorreta()
        );

        resposta.setCorrigida(true);
        resposta.setCorreta(correta);
        resposta.setPontuacaoObtida(
                correta
                        ? resposta.getQuestao().getPontuacao()
                        : BigDecimal.ZERO
        );
    }

    private void finalizarAposUltimaCorrecao(
            TentativaAvaliacao tentativa) {

        List<RespostaQuestao> respostas = respostaRepository
                .findAllByTentativaIdOrderByQuestaoOrdemAsc(
                        tentativa.getId()
                );

        boolean possuiDissertativaPendente = respostas.stream()
                .filter(resposta ->
                        resposta.getQuestao().getTipo()
                                == TipoQuestao.DISSERTATIVA
                )
                .anyMatch(resposta ->
                        !Boolean.TRUE.equals(
                                resposta.getCorrigida()
                        )
                );

        tentativa.setPontuacaoObtida(
                calcularPontuacaoObtida(respostas)
        );

        if (!possuiDissertativaPendente) {
            concluirTentativa(
                    tentativa,
                    LocalDateTime.now()
            );
        }

        tentativaRepository.save(tentativa);
    }

    private void concluirTentativa(
            TentativaAvaliacao tentativa,
            LocalDateTime dataFinalizacao) {

        BigDecimal pontuacaoTotal =
                tentativa.getPontuacaoTotal();

        if (pontuacaoTotal == null
                || pontuacaoTotal.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "A pontuação total da avaliação deve ser maior que zero."
            );
        }

        BigDecimal nota = tentativa.getPontuacaoObtida()
                .multiply(NOTA_MAXIMA)
                .divide(
                        pontuacaoTotal,
                        2,
                        RoundingMode.HALF_UP
                );

        tentativa.setNota(nota);
        tentativa.setAprovado(
                nota.compareTo(
                        tentativa.getAvaliacao().getNotaMinima()
                ) >= 0
        );
        tentativa.setStatus(StatusTentativa.FINALIZADA);
        tentativa.setDataFinalizacao(dataFinalizacao);
    }

    private BigDecimal calcularPontuacaoTotal(
            List<Questao> questoes) {

        return questoes.stream()
                .map(Questao::getPontuacao)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calcularPontuacaoObtida(
            List<RespostaQuestao> respostas) {

        return respostas.stream()
                .map(RespostaQuestao::getPontuacaoObtida)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return null;
        }

        String textoNormalizado = texto.trim();

        return textoNormalizado.isEmpty()
                ? null
                : textoNormalizado;
    }
}
