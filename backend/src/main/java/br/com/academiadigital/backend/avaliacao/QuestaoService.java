package br.com.academiadigital.backend.avaliacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.avaliacao.dto.QuestaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoUpdateRequest;
import br.com.academiadigital.backend.avaliacao.mapper.QuestaoMapper;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@Service
public class QuestaoService {

    private final QuestaoRepository questaoRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final QuestaoMapper questaoMapper;

    public QuestaoService(
            QuestaoRepository questaoRepository,
            AvaliacaoRepository avaliacaoRepository,
            QuestaoMapper questaoMapper) {

        this.questaoRepository = questaoRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.questaoMapper = questaoMapper;
    }

    @Transactional
    public QuestaoResponse criar(
            QuestaoRequest request) {

        Avaliacao avaliacao =
                buscarAvaliacaoPorId(
                        request.getAvaliacaoId()
                );

        validarOrdemDuplicadaNaCriacao(
                request.getAvaliacaoId(),
                request.getOrdem()
        );

        Questao questao =
                questaoMapper.toEntity(
                        request,
                        avaliacao
                );

        Questao questaoSalva =
                questaoRepository.save(questao);

        return questaoMapper.toResponse(
                questaoSalva
        );
    }

    @Transactional(readOnly = true)
    public Page<QuestaoResponse> listarTodos(
            Long avaliacaoId,
            TipoQuestao tipo,
            String enunciado,
            Pageable pageable) {

        Specification<Questao> filtros =
                QuestaoSpecification.comFiltros(
                        avaliacaoId,
                        tipo,
                        enunciado
                );

        return questaoRepository
                .findAll(filtros, pageable)
                .map(questaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public QuestaoResponse buscarPorId(Long id) {
        return questaoMapper.toResponse(
                buscarQuestaoPorId(id)
        );
    }

    @Transactional
    public QuestaoResponse atualizar(
            Long id,
            QuestaoUpdateRequest request) {

        Questao questao =
                buscarQuestaoPorId(id);

        Avaliacao avaliacao =
                buscarAvaliacaoPorId(
                        request.getAvaliacaoId()
                );

        validarOrdemDuplicadaNaAtualizacao(
                id,
                request.getAvaliacaoId(),
                request.getOrdem()
        );

        questaoMapper.updateEntity(
                questao,
                request,
                avaliacao
        );

        Questao questaoAtualizada =
                questaoRepository.save(questao);

        return questaoMapper.toResponse(
                questaoAtualizada
        );
    }

    @Transactional
    public void excluir(Long id) {
        Questao questao =
                buscarQuestaoPorId(id);

        questaoRepository.delete(questao);
    }

    private Questao buscarQuestaoPorId(Long id) {
        return questaoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Questão não encontrada com o ID: "
                                        + id
                        )
                );
    }

    private Avaliacao buscarAvaliacaoPorId(
            Long avaliacaoId) {

        return avaliacaoRepository
                .findById(avaliacaoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Avaliação não encontrada com o ID: "
                                        + avaliacaoId
                        )
                );
    }

    private void validarOrdemDuplicadaNaCriacao(
            Long avaliacaoId,
            Integer ordem) {

        if (questaoRepository
                .existsByAvaliacaoIdAndOrdem(
                        avaliacaoId,
                        ordem
                )) {

            throw new IllegalArgumentException(
                    "Já existe uma questão "
                            + "com esta ordem na avaliação."
            );
        }
    }

    private void validarOrdemDuplicadaNaAtualizacao(
            Long questaoId,
            Long avaliacaoId,
            Integer ordem) {

        if (questaoRepository
                .existsByAvaliacaoIdAndOrdemAndIdNot(
                        avaliacaoId,
                        ordem,
                        questaoId
                )) {

            throw new IllegalArgumentException(
                    "Já existe uma questão "
                            + "com esta ordem na avaliação."
            );
        }
    }
}
