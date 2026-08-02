package br.com.academiadigital.backend.avaliacao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoUpdateRequest;
import br.com.academiadigital.backend.avaliacao.mapper.AvaliacaoMapper;
import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.CursoRepository;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final CursoRepository cursoRepository;
    private final AvaliacaoMapper avaliacaoMapper;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            CursoRepository cursoRepository,
            AvaliacaoMapper avaliacaoMapper) {

        this.avaliacaoRepository = avaliacaoRepository;
        this.cursoRepository = cursoRepository;
        this.avaliacaoMapper = avaliacaoMapper;
    }

    @Transactional
    public AvaliacaoResponse criar(
            AvaliacaoRequest request) {

        Curso curso =
                buscarCursoPorId(request.getCursoId());

        validarOrdemDuplicadaNaCriacao(
                request.getCursoId(),
                request.getOrdem()
        );

        Avaliacao avaliacao =
                avaliacaoMapper.toEntity(
                        request,
                        curso
                );

        if (avaliacao.getStatus() == null) {
            avaliacao.setStatus(
                    StatusAvaliacao.RASCUNHO
            );
        }

        Avaliacao avaliacaoSalva =
                avaliacaoRepository.save(avaliacao);

        return avaliacaoMapper.toResponse(
                avaliacaoSalva
        );
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoResponse> listarTodos(
            Long cursoId,
            StatusAvaliacao status,
            String titulo,
            Pageable pageable) {

        Specification<Avaliacao> filtros =
                AvaliacaoSpecification.comFiltros(
                        cursoId,
                        status,
                        titulo
                );

        return avaliacaoRepository
                .findAll(filtros, pageable)
                .map(avaliacaoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponse buscarPorId(Long id) {
        return avaliacaoMapper.toResponse(
                buscarAvaliacaoPorId(id)
        );
    }

    @Transactional
    public AvaliacaoResponse atualizar(
            Long id,
            AvaliacaoUpdateRequest request) {

        Avaliacao avaliacao =
                buscarAvaliacaoPorId(id);

        Curso curso =
                buscarCursoPorId(request.getCursoId());

        validarOrdemDuplicadaNaAtualizacao(
                id,
                request.getCursoId(),
                request.getOrdem()
        );

        avaliacaoMapper.updateEntity(
                avaliacao,
                request,
                curso
        );

        Avaliacao avaliacaoAtualizada =
                avaliacaoRepository.save(avaliacao);

        return avaliacaoMapper.toResponse(
                avaliacaoAtualizada
        );
    }

    @Transactional
    public void excluir(Long id) {
        Avaliacao avaliacao =
                buscarAvaliacaoPorId(id);

        avaliacaoRepository.delete(avaliacao);
    }

    private Avaliacao buscarAvaliacaoPorId(Long id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Avaliação não encontrada com o ID: "
                                        + id
                        )
                );
    }

    private Curso buscarCursoPorId(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Curso não encontrado com o ID: "
                                        + cursoId
                        )
                );
    }

    private void validarOrdemDuplicadaNaCriacao(
            Long cursoId,
            Integer ordem) {

        if (avaliacaoRepository
                .existsByCursoIdAndOrdem(
                        cursoId,
                        ordem
                )) {

            throw new IllegalArgumentException(
                    "Já existe uma avaliação "
                            + "com esta ordem no curso."
            );
        }
    }

    private void validarOrdemDuplicadaNaAtualizacao(
            Long avaliacaoId,
            Long cursoId,
            Integer ordem) {

        if (avaliacaoRepository
                .existsByCursoIdAndOrdemAndIdNot(
                        cursoId,
                        ordem,
                        avaliacaoId
                )) {

            throw new IllegalArgumentException(
                    "Já existe uma avaliação "
                            + "com esta ordem no curso."
            );
        }
    }
}
