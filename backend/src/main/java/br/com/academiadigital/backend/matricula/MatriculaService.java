package br.com.academiadigital.backend.matricula;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.curso.CursoRepository;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.matricula.dto.MatriculaRequest;
import br.com.academiadigital.backend.matricula.dto.MatriculaResponse;
import br.com.academiadigital.backend.matricula.dto.MatriculaStatusUpdateRequest;
import br.com.academiadigital.backend.matricula.mapper.MatriculaMapper;
import br.com.academiadigital.backend.usuario.Perfil;
import br.com.academiadigital.backend.usuario.Usuario;
import br.com.academiadigital.backend.usuario.UsuarioRepository;

@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CursoRepository cursoRepository;
    private final MatriculaMapper matriculaMapper;

    public MatriculaService(
            MatriculaRepository matriculaRepository,
            UsuarioRepository usuarioRepository,
            CursoRepository cursoRepository,
            MatriculaMapper matriculaMapper) {

        this.matriculaRepository = matriculaRepository;
        this.usuarioRepository = usuarioRepository;
        this.cursoRepository = cursoRepository;
        this.matriculaMapper = matriculaMapper;
    }

    @Transactional
    public MatriculaResponse criar(MatriculaRequest request) {
        Usuario aluno = buscarAlunoPorId(
                request.getAlunoId()
        );

        validarAluno(aluno);

        Curso curso = buscarCursoPorId(
                request.getCursoId()
        );

        validarMatriculaDuplicadaNaCriacao(
                request.getAlunoId(),
                request.getCursoId()
        );

        Matricula matricula = matriculaMapper.toEntity(
                request,
                aluno,
                curso
        );

        Matricula matriculaSalva =
                matriculaRepository.save(matricula);

        return matriculaMapper.toResponse(
                matriculaSalva
        );
    }

    @Transactional(readOnly = true)
    public Page<MatriculaResponse> listarTodos(
            Long alunoId,
            Long cursoId,
            StatusMatricula status,
            Pageable pageable) {

        Specification<Matricula> filtros =
                MatriculaSpecification.comFiltros(
                        alunoId,
                        cursoId,
                        status
                );

        return matriculaRepository
                .findAll(filtros, pageable)
                .map(matriculaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<MatriculaResponse> listarMinhas(
            String email,
            Pageable pageable) {

        Usuario aluno = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aluno autenticado não encontrado."
                        )
                );

        if (aluno.getPerfil() != Perfil.ALUNO) {
            throw new IllegalArgumentException(
                    "O usuário autenticado não possui o perfil ALUNO."
            );
        }

        Specification<Matricula> filtros =
                MatriculaSpecification.comFiltros(
                        aluno.getId(),
                        null,
                        null
                );

        return matriculaRepository
                .findAll(filtros, pageable)
                .map(matriculaMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public MatriculaResponse buscarPorId(Long id) {
        return matriculaMapper.toResponse(
                buscarMatriculaPorId(id)
        );
    }

    @Transactional
    public MatriculaResponse atualizarStatus(
            Long id,
            MatriculaStatusUpdateRequest request) {

        Matricula matricula =
                buscarMatriculaPorId(id);

        StatusMatricula statusAtual =
                matricula.getStatus();

        StatusMatricula novoStatus =
                request.getStatus();

        validarTransicaoDeStatus(
                statusAtual,
                novoStatus
        );

        matricula.setStatus(novoStatus);

        atualizarDatasDeStatus(
                matricula,
                novoStatus
        );

        Matricula matriculaAtualizada =
                matriculaRepository.save(matricula);

        return matriculaMapper.toResponse(
                matriculaAtualizada
        );
    }

    private Matricula buscarMatriculaPorId(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Matrícula não encontrada com o ID: "
                                        + id
                        )
                );
    }

    private Usuario buscarAlunoPorId(Long alunoId) {
        return usuarioRepository.findById(alunoId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aluno não encontrado com o ID: "
                                        + alunoId
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

    private void validarAluno(Usuario aluno) {
        if (aluno.getPerfil() != Perfil.ALUNO) {
            throw new IllegalArgumentException(
                    "O usuário informado não possui o perfil ALUNO."
            );
        }

        if (!Boolean.TRUE.equals(aluno.getAtivo())) {
            throw new IllegalArgumentException(
                    "Não é possível matricular um aluno inativo."
            );
        }
    }

    private void validarMatriculaDuplicadaNaCriacao(
            Long alunoId,
            Long cursoId) {

        if (matriculaRepository.existsByAlunoIdAndCursoId(
                alunoId,
                cursoId
        )) {
            throw new IllegalArgumentException(
                    "O aluno já está matriculado neste curso."
            );
        }
    }

    private void validarTransicaoDeStatus(
            StatusMatricula statusAtual,
            StatusMatricula novoStatus) {

        if (statusAtual == novoStatus) {
            return;
        }

        boolean transicaoPermitida =
                statusAtual == StatusMatricula.ATIVA
                        && (
                        novoStatus == StatusMatricula.CONCLUIDA
                                || novoStatus
                                == StatusMatricula.CANCELADA
                );

        if (!transicaoPermitida) {
            throw new IllegalArgumentException(
                    "Não é possível alterar uma matrícula "
                            + statusAtual
                            + " para "
                            + novoStatus
                            + "."
            );
        }
    }

    private void atualizarDatasDeStatus(
            Matricula matricula,
            StatusMatricula status) {

        LocalDateTime agora = LocalDateTime.now();

        if (status == StatusMatricula.ATIVA) {
            matricula.setDataConclusao(null);
            matricula.setDataCancelamento(null);
            return;
        }

        if (status == StatusMatricula.CONCLUIDA) {
            if (matricula.getDataConclusao() == null) {
                matricula.setDataConclusao(agora);
            }

            matricula.setDataCancelamento(null);
            return;
        }

        if (status == StatusMatricula.CANCELADA) {
            if (matricula.getDataCancelamento() == null) {
                matricula.setDataCancelamento(agora);
            }

            matricula.setDataConclusao(null);
        }
    }
}