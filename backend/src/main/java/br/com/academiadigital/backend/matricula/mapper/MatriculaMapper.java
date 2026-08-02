package br.com.academiadigital.backend.matricula.mapper;

import org.springframework.stereotype.Component;

import br.com.academiadigital.backend.curso.Curso;
import br.com.academiadigital.backend.matricula.Matricula;
import br.com.academiadigital.backend.matricula.StatusMatricula;
import br.com.academiadigital.backend.matricula.dto.MatriculaRequest;
import br.com.academiadigital.backend.matricula.dto.MatriculaResponse;
import br.com.academiadigital.backend.usuario.Usuario;

@Component
public class MatriculaMapper {

    public Matricula toEntity(
            MatriculaRequest request,
            Usuario aluno,
            Curso curso) {

        Matricula matricula = new Matricula();

        matricula.setAluno(aluno);
        matricula.setCurso(curso);
        matricula.setStatus(StatusMatricula.ATIVA);

        return matricula;
    }

    public MatriculaResponse toResponse(
            Matricula matricula) {

        MatriculaResponse response =
                new MatriculaResponse();

        response.setId(matricula.getId());

        response.setAlunoId(
                matricula.getAluno().getId()
        );

        response.setAlunoNome(
                matricula.getAluno().getNome()
        );

        response.setAlunoEmail(
                matricula.getAluno().getEmail()
        );

        response.setCursoId(
                matricula.getCurso().getId()
        );

        response.setCursoTitulo(
                matricula.getCurso().getTitulo()
        );

        response.setStatus(
                matricula.getStatus()
        );

        response.setDataMatricula(
                matricula.getDataMatricula()
        );

        response.setDataConclusao(
                matricula.getDataConclusao()
        );

        response.setDataCancelamento(
                matricula.getDataCancelamento()
        );

        response.setDataAtualizacao(
                matricula.getDataAtualizacao()
        );

        return response;
    }
}