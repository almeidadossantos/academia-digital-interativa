package br.com.academiadigital.backend.progresso;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.aula.Aula;
import br.com.academiadigital.backend.aula.AulaRepository;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.matricula.Matricula;
import br.com.academiadigital.backend.matricula.MatriculaRepository;
import br.com.academiadigital.backend.progresso.dto.ProgressoAulaResponse;
import br.com.academiadigital.backend.progresso.dto.ProgressoCursoResponse;
import br.com.academiadigital.backend.progresso.mapper.ProgressoAulaMapper;
import br.com.academiadigital.backend.usuario.Perfil;
import br.com.academiadigital.backend.usuario.Usuario;
import br.com.academiadigital.backend.usuario.UsuarioRepository;

@Service
public class ProgressoService {

    private final UsuarioRepository usuarioRepository;

    private final MatriculaRepository matriculaRepository;

    private final AulaRepository aulaRepository;

    private final ProgressoAulaRepository progressoAulaRepository;

    private final ProgressoAulaMapper progressoAulaMapper;

    public ProgressoService(
            UsuarioRepository usuarioRepository,
            MatriculaRepository matriculaRepository,
            AulaRepository aulaRepository,
            ProgressoAulaRepository progressoAulaRepository,
            ProgressoAulaMapper progressoAulaMapper) {

        this.usuarioRepository = usuarioRepository;
        this.matriculaRepository = matriculaRepository;
        this.aulaRepository = aulaRepository;
        this.progressoAulaRepository =
                progressoAulaRepository;
        this.progressoAulaMapper =
                progressoAulaMapper;
    }

    @Transactional(readOnly = true)
    public ProgressoCursoResponse buscarProgressoCurso(
            String email,
            Long cursoId) {

        Usuario aluno =
                buscarAlunoAutenticado(email);

        Matricula matricula =
                buscarMatriculaDoAluno(
                        aluno.getId(),
                        cursoId
                );

        List<Aula> aulas =
                aulaRepository
                        .findAllByCursoIdOrderByOrdemAsc(
                                cursoId
                        );

        Map<Long, ProgressoAula> progressosPorAula =
                progressoAulaRepository
                        .findAllByMatriculaIdOrderByAulaOrdemAsc(
                                matricula.getId()
                        )
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        progresso ->
                                                progresso
                                                        .getAula()
                                                        .getId(),
                                        Function.identity()
                                )
                        );

        List<ProgressoAulaResponse> aulasResponse =
                aulas.stream()
                        .map(aula ->
                                obterRespostaDaAula(
                                        matricula,
                                        aula,
                                        progressosPorAula
                                )
                        )
                        .toList();

        long totalAulas =
                aulasResponse.size();

        long aulasConcluidas =
                aulasResponse.stream()
                        .filter(response ->
                                Boolean.TRUE.equals(
                                        response.getConcluida()
                                )
                        )
                        .count();

        ProgressoCursoResponse response =
                new ProgressoCursoResponse();

        response.setMatriculaId(
                matricula.getId()
        );

        response.setCursoId(
                matricula.getCurso().getId()
        );

        response.setCursoTitulo(
                matricula.getCurso().getTitulo()
        );

        response.setTotalAulas(
                totalAulas
        );

        response.setAulasConcluidas(
                aulasConcluidas
        );

        response.setPercentualConclusao(
                calcularPercentualConclusao(
                        totalAulas,
                        aulasConcluidas
                )
        );

        response.setAulas(
                aulasResponse
        );

        return response;
    }

    @Transactional
    public ProgressoAulaResponse concluirAula(
            String email,
            Long aulaId) {

        Usuario aluno =
                buscarAlunoAutenticado(email);

        Aula aula =
                buscarAula(aulaId);

        Matricula matricula =
                buscarMatriculaDoAluno(
                        aluno.getId(),
                        aula.getCurso().getId()
                );

        ProgressoAula progresso =
                progressoAulaRepository
                        .findByMatriculaIdAndAulaId(
                                matricula.getId(),
                                aulaId
                        )
                        .orElseGet(() ->
                                criarProgressoInicial(
                                        matricula,
                                        aula
                                )
                        );

        progresso.setConcluida(true);

        ProgressoAula progressoSalvo =
                progressoAulaRepository.save(
                        progresso
                );

        return progressoAulaMapper.toResponse(
                progressoSalvo
        );
    }

    @Transactional
    public void removerConclusaoAula(
            String email,
            Long aulaId) {

        Usuario aluno =
                buscarAlunoAutenticado(email);

        Aula aula =
                buscarAula(aulaId);

        Matricula matricula =
                buscarMatriculaDoAluno(
                        aluno.getId(),
                        aula.getCurso().getId()
                );

        ProgressoAula progresso =
                progressoAulaRepository
                        .findByMatriculaIdAndAulaId(
                                matricula.getId(),
                                aulaId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Conclusão não encontrada "
                                                + "para a aula de ID: "
                                                + aulaId
                                )
                        );

        progresso.setConcluida(false);

        progressoAulaRepository.save(
                progresso
        );
    }

    private ProgressoAulaResponse obterRespostaDaAula(
            Matricula matricula,
            Aula aula,
            Map<Long, ProgressoAula> progressosPorAula) {

        ProgressoAula progresso =
                progressosPorAula.get(
                        aula.getId()
                );

        if (progresso == null) {
            progresso =
                    criarProgressoInicial(
                            matricula,
                            aula
                    );
        }

        return progressoAulaMapper.toResponse(
                progresso
        );
    }

    private ProgressoAula criarProgressoInicial(
            Matricula matricula,
            Aula aula) {

        ProgressoAula progresso =
                new ProgressoAula();

        progresso.setMatricula(matricula);
        progresso.setAula(aula);
        progresso.setConcluida(false);

        return progresso;
    }

    private Aula buscarAula(Long aulaId) {

        return aulaRepository
                .findById(aulaId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Aula não encontrada com ID: "
                                        + aulaId
                        )
                );
    }

    private Usuario buscarAlunoAutenticado(
            String email) {

        Usuario aluno =
                usuarioRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Aluno autenticado não encontrado."
                                )
                        );

        if (aluno.getPerfil() != Perfil.ALUNO) {
            throw new IllegalArgumentException(
                    "O usuário autenticado não possui "
                            + "o perfil ALUNO."
            );
        }

        return aluno;
    }

    private Matricula buscarMatriculaDoAluno(
            Long alunoId,
            Long cursoId) {

        return matriculaRepository
                .findByAlunoIdAndCursoId(
                        alunoId,
                        cursoId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Matrícula não encontrada "
                                        + "para o aluno autenticado "
                                        + "no curso de ID: "
                                        + cursoId
                        )
                );
    }

    private double calcularPercentualConclusao(
            long totalAulas,
            long aulasConcluidas) {

        if (totalAulas == 0) {
            return 0.0;
        }

        double percentual =
                aulasConcluidas
                        * 100.0
                        / totalAulas;

        return Math.round(
                percentual * 100.0
        ) / 100.0;
    }
}
