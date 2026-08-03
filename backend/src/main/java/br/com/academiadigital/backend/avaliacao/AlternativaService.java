package br.com.academiadigital.backend.avaliacao;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.avaliacao.dto.AlternativaRequest;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaResponse;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaUpdateRequest;
import br.com.academiadigital.backend.avaliacao.mapper.AlternativaMapper;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@Service
public class AlternativaService {

    private final AlternativaRepository alternativaRepository;
    private final QuestaoRepository questaoRepository;
    private final AlternativaMapper alternativaMapper;

    public AlternativaService(
            AlternativaRepository alternativaRepository,
            QuestaoRepository questaoRepository,
            AlternativaMapper alternativaMapper) {

        this.alternativaRepository = alternativaRepository;
        this.questaoRepository = questaoRepository;
        this.alternativaMapper = alternativaMapper;
    }

    @Transactional
    public AlternativaResponse criar(
            AlternativaRequest request) {

        Questao questao =
                buscarQuestaoPorId(
                        request.getQuestaoId()
                );

        validarTipoDaQuestao(questao);

        validarOrdemDuplicadaNaCriacao(
                request.getQuestaoId(),
                request.getOrdem()
        );

        Alternativa alternativa =
                alternativaMapper.toEntity(
                        request,
                        questao
                );

        Alternativa alternativaSalva =
                alternativaRepository.save(
                        alternativa
                );

        return alternativaMapper.toResponse(
                alternativaSalva
        );
    }

    @Transactional(readOnly = true)
    public List<AlternativaResponse> listarPorQuestao(
            Long questaoId) {

        buscarQuestaoPorId(questaoId);

        return alternativaRepository
                .findAllByQuestaoIdOrderByOrdemAsc(
                        questaoId
                )
                .stream()
                .map(alternativaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AlternativaResponse buscarPorId(Long id) {
        return alternativaMapper.toResponse(
                buscarAlternativaPorId(id)
        );
    }

    @Transactional
    public AlternativaResponse atualizar(
            Long id,
            AlternativaUpdateRequest request) {

        Alternativa alternativa =
                buscarAlternativaPorId(id);

        Questao questao =
                buscarQuestaoPorId(
                        request.getQuestaoId()
                );

        validarTipoDaQuestao(questao);

        validarOrdemDuplicadaNaAtualizacao(
                id,
                request.getQuestaoId(),
                request.getOrdem()
        );

        alternativaMapper.updateEntity(
                alternativa,
                request,
                questao
        );

        Alternativa alternativaAtualizada =
                alternativaRepository.save(
                        alternativa
                );

        return alternativaMapper.toResponse(
                alternativaAtualizada
        );
    }

    @Transactional
    public void excluir(Long id) {
        Alternativa alternativa =
                buscarAlternativaPorId(id);

        alternativaRepository.delete(
                alternativa
        );
    }

    private Alternativa buscarAlternativaPorId(Long id) {
        return alternativaRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Alternativa não encontrada com o ID: "
                                        + id
                        )
                );
    }

    private Questao buscarQuestaoPorId(Long questaoId) {
        return questaoRepository
                .findById(questaoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Questão não encontrada com o ID: "
                                        + questaoId
                        )
                );
    }

    private void validarTipoDaQuestao(
            Questao questao) {

        if (questao.getTipo()
                == TipoQuestao.DISSERTATIVA) {

            throw new IllegalArgumentException(
                    "Questões dissertativas "
                            + "não podem possuir alternativas."
            );
        }
    }

    private void validarOrdemDuplicadaNaCriacao(
            Long questaoId,
            Integer ordem) {

        if (alternativaRepository
                .existsByQuestaoIdAndOrdem(
                        questaoId,
                        ordem
                )) {

            throw new IllegalArgumentException(
                    "Já existe uma alternativa "
                            + "com esta ordem na questão."
            );
        }
    }

    private void validarOrdemDuplicadaNaAtualizacao(
            Long alternativaId,
            Long questaoId,
            Integer ordem) {

        if (alternativaRepository
                .existsByQuestaoIdAndOrdemAndIdNot(
                        questaoId,
                        ordem,
                        alternativaId
                )) {

            throw new IllegalArgumentException(
                    "Já existe uma alternativa "
                            + "com esta ordem na questão."
            );
        }
    }
}
