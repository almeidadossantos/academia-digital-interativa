package br.com.academiadigital.backend.aula;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.aula.dto.AulaRequest;
import br.com.academiadigital.backend.aula.dto.AulaResponse;
import br.com.academiadigital.backend.aula.dto.AulaUpdateRequest;
import br.com.academiadigital.backend.aula.mapper.AulaMapper;
import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.CursoRepository;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@Service
public class AulaService {

    private final AulaRepository aulaRepository;
    private final CursoRepository cursoRepository;
    private final AulaMapper aulaMapper;

    public AulaService(
            AulaRepository aulaRepository,
            CursoRepository cursoRepository,
            AulaMapper aulaMapper) {

        this.aulaRepository = aulaRepository;
        this.cursoRepository = cursoRepository;
        this.aulaMapper = aulaMapper;
    }

    @Transactional
    public AulaResponse criar(AulaRequest request) {
        Curso curso = buscarCursoPorId(request.getCursoId());

        validarOrdemDuplicadaNaCriacao(
                request.getCursoId(),
                request.getOrdem()
        );

        Aula aula = aulaMapper.toEntity(request, curso);

        if (aula.getStatus() == null) {
            aula.setStatus(StatusAula.RASCUNHO);
        }

        Aula aulaSalva = aulaRepository.save(aula);

        return aulaMapper.toResponse(aulaSalva);
    }

    @Transactional(readOnly = true)
    public Page<AulaResponse> listarTodos(
            Long cursoId,
            StatusAula status,
            String titulo,
            Pageable pageable) {

        Specification<Aula> filtros =
                AulaSpecification.comFiltros(
                        cursoId,
                        status,
                        titulo
                );

        return aulaRepository.findAll(filtros, pageable)
                .map(aulaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AulaResponse buscarPorId(Long id) {
        return aulaMapper.toResponse(buscarAulaPorId(id));
    }

    @Transactional
    public AulaResponse atualizar(
            Long id,
            AulaUpdateRequest request) {

        Aula aula = buscarAulaPorId(id);
        Curso curso = buscarCursoPorId(request.getCursoId());

        validarOrdemDuplicadaNaAtualizacao(
                id,
                request.getCursoId(),
                request.getOrdem()
        );

        aulaMapper.updateEntity(aula, request, curso);

        Aula aulaAtualizada = aulaRepository.save(aula);

        return aulaMapper.toResponse(aulaAtualizada);
    }

    @Transactional
    public void excluir(Long id) {
        Aula aula = buscarAulaPorId(id);
        aulaRepository.delete(aula);
    }

    private Aula buscarAulaPorId(Long id) {
        return aulaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aula não encontrada com o ID: " + id
                        )
                );
    }

    private Curso buscarCursoPorId(Long cursoId) {
        return cursoRepository.findById(cursoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Curso não encontrado com o ID: " + cursoId
                        )
                );
    }

    private void validarOrdemDuplicadaNaCriacao(
            Long cursoId,
            Integer ordem) {

        if (aulaRepository.existsByCursoIdAndOrdem(
                cursoId,
                ordem
        )) {
            throw new IllegalArgumentException(
                    "Já existe uma aula com esta ordem no curso."
            );
        }
    }

    private void validarOrdemDuplicadaNaAtualizacao(
            Long aulaId,
            Long cursoId,
            Integer ordem) {

        if (aulaRepository.existsByCursoIdAndOrdemAndIdNot(
                cursoId,
                ordem,
                aulaId
        )) {
            throw new IllegalArgumentException(
                    "Já existe uma aula com esta ordem no curso."
            );
        }
    }
}