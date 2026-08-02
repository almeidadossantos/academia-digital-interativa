package br.com.academiadigital.backend.progresso;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.progresso.dto.ProgressoAulaResponse;
import br.com.academiadigital.backend.progresso.dto.ProgressoCursoResponse;
import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtService;

@WebMvcTest(ProgressoController.class)
class ProgressoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProgressoService progressoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void deveBuscarProgressoDoCursoDoAlunoAutenticado()
            throws Exception {

        ProgressoAulaResponse aula =
                new ProgressoAulaResponse();

        aula.setAulaId(30L);
        aula.setAulaTitulo("Introdução");
        aula.setAulaOrdem(1);
        aula.setConcluida(true);

        ProgressoCursoResponse response =
                new ProgressoCursoResponse();

        response.setMatriculaId(20L);
        response.setCursoId(10L);
        response.setCursoTitulo(
                "Informática Básica"
        );
        response.setTotalAulas(2);
        response.setAulasConcluidas(1);
        response.setPercentualConclusao(50.0);
        response.setAulas(List.of(aula));

        when(
                progressoService.buscarProgressoCurso(
                        "aluno@email.com",
                        10L
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/progressos/cursos/{cursoId}",
                                10L
                        )
                                .principal(
                                        () -> "aluno@email.com"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaId")
                        .value(20))
                .andExpect(jsonPath("$.cursoId")
                        .value(10))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Informática Básica"))
                .andExpect(jsonPath("$.totalAulas")
                        .value(2))
                .andExpect(jsonPath("$.aulasConcluidas")
                        .value(1))
                .andExpect(jsonPath("$.percentualConclusao")
                        .value(50.0))
                .andExpect(jsonPath("$.aulas").isArray())
                .andExpect(jsonPath("$.aulas[0].aulaId")
                        .value(30))
                .andExpect(jsonPath("$.aulas[0].concluida")
                        .value(true));

        verify(progressoService)
                .buscarProgressoCurso(
                        "aluno@email.com",
                        10L
                );
    }

    @Test
    void deveRetornarNotFoundQuandoAlunoNaoEstiverMatriculado()
            throws Exception {

        when(
                progressoService.buscarProgressoCurso(
                        "aluno@email.com",
                        99L
                )
        ).thenThrow(
                new ResourceNotFoundException(
                        "Matrícula não encontrada para o aluno "
                                + "autenticado no curso de ID: 99"
                )
        );

        mockMvc.perform(
                        get(
                                "/api/v1/progressos/cursos/{cursoId}",
                                99L
                        )
                                .principal(
                                        () -> "aluno@email.com"
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Matrícula não encontrada para o aluno "
                                        + "autenticado no curso de ID: 99"
                        ))
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/v1/progressos/cursos/99"
                        ));

        verify(progressoService)
                .buscarProgressoCurso(
                        "aluno@email.com",
                        99L
                );
    }
    @Test
    void deveConcluirAulaDoAlunoAutenticado()
            throws Exception {

        ProgressoAulaResponse response =
                new ProgressoAulaResponse();

        response.setId(40L);
        response.setMatriculaId(20L);
        response.setCursoId(10L);
        response.setCursoTitulo(
                "Informática Básica"
        );
        response.setAulaId(30L);
        response.setAulaTitulo(
                "Introdução"
        );
        response.setAulaOrdem(1);
        response.setConcluida(true);

        when(
                progressoService.concluirAula(
                        "aluno@email.com",
                        30L
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post(
                                "/api/v1/progressos/aulas/{aulaId}/concluir",
                                30L
                        )
                                .principal(
                                        () -> "aluno@email.com"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(40))
                .andExpect(jsonPath("$.matriculaId")
                        .value(20))
                .andExpect(jsonPath("$.cursoId")
                        .value(10))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Informática Básica"))
                .andExpect(jsonPath("$.aulaId")
                        .value(30))
                .andExpect(jsonPath("$.aulaTitulo")
                        .value("Introdução"))
                .andExpect(jsonPath("$.aulaOrdem")
                        .value(1))
                .andExpect(jsonPath("$.concluida")
                        .value(true));

        verify(progressoService)
                .concluirAula(
                        "aluno@email.com",
                        30L
                );
    }

    @Test
    void deveRetornarNotFoundAoConcluirAulaInexistente()
            throws Exception {

        when(
                progressoService.concluirAula(
                        "aluno@email.com",
                        999L
                )
        ).thenThrow(
                new ResourceNotFoundException(
                        "Aula não encontrada com ID: 999"
                )
        );

        mockMvc.perform(
                        post(
                                "/api/v1/progressos/aulas/{aulaId}/concluir",
                                999L
                        )
                                .principal(
                                        () -> "aluno@email.com"
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Aula não encontrada com ID: 999"
                        ))
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/v1/progressos/aulas/999/concluir"
                        ));

        verify(progressoService)
                .concluirAula(
                        "aluno@email.com",
                        999L
                );
    }

}
