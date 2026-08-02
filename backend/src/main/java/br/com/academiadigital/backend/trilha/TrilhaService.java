package br.com.academiadigital.backend.trilha;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class TrilhaService {

    private final TrilhaRepository trilhaRepository;

    private final TrilhaCursoRepository
            trilhaCursoRepository;

    private final CursoRepository cursoRepository;

    private final TrilhaMapper trilhaMapper;

    private final TrilhaCursoMapper
            trilhaCursoMapper;

    public TrilhaService(
            TrilhaRepository trilhaRepository,
            TrilhaCursoRepository trilhaCursoRepository,
            CursoRepository cursoRepository,
            TrilhaMapper trilhaMapper,
            TrilhaCursoMapper trilhaCursoMapper) {

        this.trilhaRepository = trilhaRepository;

        this.trilhaCursoRepository =
                trilhaCursoRepository;

        this.cursoRepository = cursoRepository;
        this.trilhaMapper = trilhaMapper;
        this.trilhaCursoMapper = trilhaCursoMapper;
    }

    @Transactional
    public TrilhaResponse criar(
            TrilhaRequest request) {

        validarTituloDuplicadoNaCriacao(
                request.getTitulo()
        );

        Trilha trilha =
                trilhaMapper.toEntity(request);

        Trilha trilhaSalva =
                trilhaRepository.save(trilha);

        return trilhaMapper.toResponse(
                trilhaSalva,
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public Page<TrilhaResponse> listarTodos(
            String titulo,
            StatusTrilha status,
            Pageable pageable) {

        Specification<Trilha> filtros =
                TrilhaSpecification.comFiltros(
                        titulo,
                        status
                );

        return trilhaRepository
                .findAll(filtros, pageable)
                .map(this::converterParaResponse);
    }

    @Transactional(readOnly = true)
    public TrilhaResponse buscarPorId(
            Long id) {

        Trilha trilha = buscarTrilhaPorId(id);

        return converterParaResponse(trilha);
    }

    @Transactional
    public TrilhaResponse atualizar(
            Long id,
            TrilhaRequest request) {

        Trilha trilha = buscarTrilhaPorId(id);

        validarTituloDuplicadoNaAtualizacao(
                request.getTitulo(),
                id
        );

        trilhaMapper.atualizarEntity(
                trilha,
                request
        );

        Trilha trilhaSalva =
                trilhaRepository.save(trilha);

        return converterParaResponse(
                trilhaSalva
        );
    }

    @Transactional
    public void excluir(Long id) {
        buscarTrilhaPorId(id);

        trilhaCursoRepository
                .deleteAllByTrilhaId(id);

        trilhaRepository.deleteById(id);
    }

    @Transactional
    public TrilhaCursoResponse adicionarCurso(
            Long trilhaId,
            TrilhaCursoRequest request) {

        Trilha trilha =
                buscarTrilhaPorId(trilhaId);

        Curso curso =
                buscarCursoPorId(
                        request.getCursoId()
                );

        validarCursoDuplicado(
                trilhaId,
                request.getCursoId()
        );

        validarOrdemDuplicadaNaCriacao(
                trilhaId,
                request.getOrdem()
        );

        TrilhaCurso trilhaCurso =
                trilhaCursoMapper.toEntity(
                        request,
                        trilha,
                        curso
                );

        TrilhaCurso trilhaCursoSalvo =
                trilhaCursoRepository.save(
                        trilhaCurso
                );

        return trilhaCursoMapper.toResponse(
                trilhaCursoSalvo
        );
    }

    @Transactional
    public TrilhaCursoResponse atualizarOrdem(
            Long trilhaId,
            Long cursoId,
            TrilhaCursoOrdemRequest request) {

        buscarTrilhaPorId(trilhaId);

        TrilhaCurso trilhaCurso =
                buscarTrilhaCurso(
                        trilhaId,
                        cursoId
                );

        validarOrdemDuplicadaNaAtualizacao(
                trilhaId,
                request.getOrdem(),
                trilhaCurso.getId()
        );

        trilhaCurso.setOrdem(
                request.getOrdem()
        );

        TrilhaCurso trilhaCursoSalvo =
                trilhaCursoRepository.save(
                        trilhaCurso
                );

        return trilhaCursoMapper.toResponse(
                trilhaCursoSalvo
        );
    }

    @Transactional
    public void removerCurso(
            Long trilhaId,
            Long cursoId) {

        buscarTrilhaPorId(trilhaId);

        TrilhaCurso trilhaCurso =
                buscarTrilhaCurso(
                        trilhaId,
                        cursoId
                );

        trilhaCursoRepository.delete(
                trilhaCurso
        );
    }

    private TrilhaResponse converterParaResponse(
            Trilha trilha) {

        List<TrilhaCurso> cursos =
                trilhaCursoRepository
                        .findAllByTrilhaIdOrderByOrdemAsc(
                                trilha.getId()
                        );

        return trilhaMapper.toResponse(
                trilha,
                cursos
        );
    }

    private Trilha buscarTrilhaPorId(
            Long id) {

        return trilhaRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Trilha não encontrada com o ID: "
                                        + id
                        )
                );
    }

    private Curso buscarCursoPorId(
            Long id) {

        return cursoRepository
                .findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Curso não encontrado com o ID: "
                                        + id
                        )
                );
    }

    private TrilhaCurso buscarTrilhaCurso(
            Long trilhaId,
            Long cursoId) {

        return trilhaCursoRepository
                .findByTrilhaIdAndCursoId(
                        trilhaId,
                        cursoId
                )
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "O curso informado não pertence "
                                        + "à trilha."
                        )
                );
    }

    private void validarTituloDuplicadoNaCriacao(
            String titulo) {

        if (trilhaRepository
                .existsByTituloIgnoreCase(
                        titulo.trim()
                )) {

            throw new IllegalArgumentException(
                    "Já existe uma trilha com "
                            + "o título informado."
            );
        }
    }

    private void validarTituloDuplicadoNaAtualizacao(
            String titulo,
            Long id) {

        if (trilhaRepository
                .existsByTituloIgnoreCaseAndIdNot(
                        titulo.trim(),
                        id
                )) {

            throw new IllegalArgumentException(
                    "Já existe uma trilha com "
                            + "o título informado."
            );
        }
    }

    private void validarCursoDuplicado(
            Long trilhaId,
            Long cursoId) {

        if (trilhaCursoRepository
                .existsByTrilhaIdAndCursoId(
                        trilhaId,
                        cursoId
                )) {

            throw new IllegalArgumentException(
                    "O curso já está associado "
                            + "a esta trilha."
            );
        }
    }

    private void validarOrdemDuplicadaNaCriacao(
            Long trilhaId,
            Integer ordem) {

        if (trilhaCursoRepository
                .existsByTrilhaIdAndOrdem(
                        trilhaId,
                        ordem
                )) {

            throw new IllegalArgumentException(
                    "Já existe um curso na ordem "
                            + "informada para esta trilha."
            );
        }
    }

    private void validarOrdemDuplicadaNaAtualizacao(
            Long trilhaId,
            Integer ordem,
            Long trilhaCursoId) {

        if (trilhaCursoRepository
                .existsByTrilhaIdAndOrdemAndIdNot(
                        trilhaId,
                        ordem,
                        trilhaCursoId
                )) {

            throw new IllegalArgumentException(
                    "Já existe um curso na ordem "
                            + "informada para esta trilha."
            );
        }
    }
}