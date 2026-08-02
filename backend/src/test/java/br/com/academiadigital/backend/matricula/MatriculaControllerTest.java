package br.com.academiadigital.backend.matricula;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.matricula.dto.MatriculaRequest;
import br.com.academiadigital.backend.matricula.dto.MatriculaResponse;
import br.com.academiadigital.backend.matricula.dto.MatriculaStatusUpdateRequest;
import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MatriculaController.class)
class MatriculaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MatriculaService matriculaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void deveCriarMatricula() throws Exception {
        MatriculaRequest request = criarRequest();

        MatriculaResponse response = criarResponse(
                10L,
                StatusMatricula.ATIVA
        );

        when(matriculaService.criar(
                any(MatriculaRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/matriculas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/v1/matriculas/10"
                ))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.alunoId").value(1))
                .andExpect(jsonPath("$.alunoNome")
                        .value("João Silva"))
                .andExpect(jsonPath("$.alunoEmail")
                        .value("joao@email.com"))
                .andExpect(jsonPath("$.cursoId").value(2))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Informática Básica"))
                .andExpect(jsonPath("$.status")
                        .value("ATIVA"));

        verify(matriculaService).criar(
                any(MatriculaRequest.class)
        );
    }

    @Test
    void deveListarMatriculasComFiltrosEPaginacao()
            throws Exception {

        MatriculaResponse matricula1 = criarResponse(
                10L,
                StatusMatricula.ATIVA
        );

        MatriculaResponse matricula2 = criarResponse(
                11L,
                StatusMatricula.ATIVA
        );

        Page<MatriculaResponse> pagina = new PageImpl<>(
                List.of(matricula1, matricula2),
                PageRequest.of(0, 10),
                2
        );

        when(matriculaService.listarTodos(
                eq(1L),
                eq(2L),
                eq(StatusMatricula.ATIVA),
                any()
        )).thenReturn(pagina);

        mockMvc.perform(
                        get("/api/v1/matriculas")
                                .param("alunoId", "1")
                                .param("cursoId", "2")
                                .param("status", "ATIVA")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()")
                        .value(2))
                .andExpect(jsonPath("$.content[0].id")
                        .value(10))
                .andExpect(jsonPath("$.content[0].status")
                        .value("ATIVA"))
                .andExpect(jsonPath("$.content[1].id")
                        .value(11))
                .andExpect(jsonPath("$.totalElements")
                        .value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(matriculaService).listarTodos(
                eq(1L),
                eq(2L),
                eq(StatusMatricula.ATIVA),
                any()
        );
    }

    @Test
    void deveListarMinhasMatriculasDoAlunoAutenticado()
            throws Exception {

        MatriculaResponse matricula = criarResponse(
                10L,
                StatusMatricula.ATIVA
        );

        Page<MatriculaResponse> pagina = new PageImpl<>(
                List.of(matricula),
                PageRequest.of(0, 10),
                1
        );

        when(matriculaService.listarMinhas(
                eq("joao@email.com"),
                any()
        )).thenReturn(pagina);

        mockMvc.perform(
                        get("/api/v1/matriculas/minhas")
                                .principal(
                                        () -> "joao@email.com"
                                )
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()")
                        .value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value(10))
                .andExpect(jsonPath("$.content[0].alunoId")
                        .value(1))
                .andExpect(jsonPath("$.content[0].alunoEmail")
                        .value("joao@email.com"))
                .andExpect(jsonPath("$.content[0].status")
                        .value("ATIVA"))
                .andExpect(jsonPath("$.totalElements")
                        .value(1));

        verify(matriculaService).listarMinhas(
                eq("joao@email.com"),
                any()
        );
    }

    @Test
    void deveBuscarMatriculaPorId() throws Exception {
        MatriculaResponse response = criarResponse(
                10L,
                StatusMatricula.ATIVA
        );

        when(matriculaService.buscarPorId(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/matriculas/{id}", 10L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.alunoId").value(1))
                .andExpect(jsonPath("$.cursoId").value(2))
                .andExpect(jsonPath("$.status")
                        .value("ATIVA"));

        verify(matriculaService).buscarPorId(10L);
    }

    @Test
    void deveAtualizarStatusDaMatricula() throws Exception {
        MatriculaStatusUpdateRequest request =
                criarStatusRequest(
                        StatusMatricula.CONCLUIDA
                );

        MatriculaResponse response = criarResponse(
                10L,
                StatusMatricula.CONCLUIDA
        );

        when(matriculaService.atualizarStatus(
                eq(10L),
                any(MatriculaStatusUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/matriculas/{id}/status",
                                10L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.alunoId").value(1))
                .andExpect(jsonPath("$.cursoId").value(2))
                .andExpect(jsonPath("$.status")
                        .value("CONCLUIDA"));

        verify(matriculaService).atualizarStatus(
                eq(10L),
                any(MatriculaStatusUpdateRequest.class)
        );
    }

    @Test
    void deveRetornarBadRequestAoCriarComDadosInvalidos()
            throws Exception {

        MatriculaRequest request =
                new MatriculaRequest();

        request.setAlunoId(0L);
        request.setCursoId(0L);

        mockMvc.perform(
                        post("/api/v1/matriculas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Error"))
                .andExpect(jsonPath("$.errors.alunoId")
                        .exists())
                .andExpect(jsonPath("$.errors.cursoId")
                        .exists());

        verify(
                matriculaService,
                never()
        ).criar(any(MatriculaRequest.class));
    }

    @Test
    void deveRetornarBadRequestAoAtualizarSemStatus()
            throws Exception {

        MatriculaStatusUpdateRequest request =
                new MatriculaStatusUpdateRequest();

        request.setStatus(null);

        mockMvc.perform(
                        patch(
                                "/api/v1/matriculas/{id}/status",
                                10L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Error"))
                .andExpect(jsonPath("$.errors.status")
                        .exists());

        verify(
                matriculaService,
                never()
        ).atualizarStatus(
                eq(10L),
                any(MatriculaStatusUpdateRequest.class)
        );
    }

    @Test
    void deveRetornarBadRequestParaTransicaoDeStatusInvalida()
            throws Exception {

        MatriculaStatusUpdateRequest request =
                criarStatusRequest(
                        StatusMatricula.ATIVA
                );

        when(matriculaService.atualizarStatus(
                eq(10L),
                any(MatriculaStatusUpdateRequest.class)
        )).thenThrow(
                new IllegalArgumentException(
                        "Não é possível alterar uma matrícula "
                                + "CANCELADA para ATIVA."
                )
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/matriculas/{id}/status",
                                10L
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Não é possível alterar uma matrícula "
                                        + "CANCELADA para ATIVA."
                        ))
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/v1/matriculas/10/status"
                        ));

        verify(matriculaService).atualizarStatus(
                eq(10L),
                any(MatriculaStatusUpdateRequest.class)
        );
    }

    @Test
    void deveRetornarNotFoundAoBuscarMatriculaInexistente()
            throws Exception {

        when(matriculaService.buscarPorId(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Matrícula não encontrada com o ID: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/matriculas/{id}", 99L)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Matrícula não encontrada com o ID: 99"
                        ))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/matriculas/99"));

        verify(matriculaService).buscarPorId(99L);
    }

    private MatriculaRequest criarRequest() {
        MatriculaRequest request =
                new MatriculaRequest();

        request.setAlunoId(1L);
        request.setCursoId(2L);

        return request;
    }

    private MatriculaStatusUpdateRequest criarStatusRequest(
            StatusMatricula status) {

        MatriculaStatusUpdateRequest request =
                new MatriculaStatusUpdateRequest();

        request.setStatus(status);

        return request;
    }

    private MatriculaResponse criarResponse(
            Long id,
            StatusMatricula status) {

        MatriculaResponse response =
                new MatriculaResponse();

        response.setId(id);
        response.setAlunoId(1L);
        response.setAlunoNome("João Silva");
        response.setAlunoEmail("joao@email.com");
        response.setCursoId(2L);
        response.setCursoTitulo("Informática Básica");
        response.setStatus(status);

        return response;
    }
}