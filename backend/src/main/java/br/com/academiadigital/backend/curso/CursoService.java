package br.com.academiadigital.backend.curso;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.curso.dto.CursoRequest;
import br.com.academiadigital.backend.curso.dto.CursoResponse;
import br.com.academiadigital.backend.curso.dto.CursoUpdateRequest;
import br.com.academiadigital.backend.curso.mapper.CursoMapper;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    @Transactional
    public CursoResponse criar(CursoRequest request) {
        String tituloNormalizado = normalizarTitulo(request.getTitulo());

        if (cursoRepository.existsByTituloIgnoreCase(tituloNormalizado)) {
            throw new IllegalArgumentException(
                    "Já existe um curso com este título."
            );
        }

        Curso curso = CursoMapper.toEntity(request);
        curso.setTitulo(tituloNormalizado);

        if (curso.getStatus() == null) {
            curso.setStatus(StatusCurso.RASCUNHO);
        }

        Curso cursoSalvo = cursoRepository.save(curso);

        return CursoMapper.toResponse(cursoSalvo);
    }

    @Transactional(readOnly = true)
    public Page<CursoResponse> listarTodos(
            String titulo,
            NivelCurso nivel,
            StatusCurso status,
            Pageable pageable) {

        Specification<Curso> filtros =
                CursoSpecification.comFiltros(
                        titulo,
                        nivel,
                        status
                );

        return cursoRepository.findAll(filtros, pageable)
                .map(CursoMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CursoResponse buscarPorId(Long id) {
        return CursoMapper.toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public CursoResponse atualizar(
            Long id,
            CursoUpdateRequest request) {

        Curso curso = buscarEntidadePorId(id);
        String tituloNormalizado = normalizarTitulo(request.getTitulo());

        validarTituloDuplicadoNaAtualizacao(
                curso,
                tituloNormalizado
        );

        CursoMapper.updateEntity(curso, request);
        curso.setTitulo(tituloNormalizado);

        Curso cursoAtualizado = cursoRepository.save(curso);

        return CursoMapper.toResponse(cursoAtualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Curso curso = buscarEntidadePorId(id);
        cursoRepository.delete(curso);
    }

    private Curso buscarEntidadePorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Curso não encontrado com o ID: " + id
                        )
                );
    }

    private void validarTituloDuplicadoNaAtualizacao(
            Curso curso,
            String novoTitulo) {

        boolean tituloFoiAlterado =
                !curso.getTitulo().equalsIgnoreCase(novoTitulo);

        if (tituloFoiAlterado
                && cursoRepository.existsByTituloIgnoreCase(novoTitulo)) {

            throw new IllegalArgumentException(
                    "Já existe um curso com este título."
            );
        }
    }

    private String normalizarTitulo(String titulo) {
        return titulo.trim();
    }
}