package br.com.academiadigital.backend.trilha;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtService;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoOrdemRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoResponse;
import br.com.academiadigital.backend.trilha.dto.TrilhaRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaResponse;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TrilhaController.class)
class TrilhaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrilhaService trilhaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint
            jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler
            jwtAccessDeniedHandler;

    @Test
    void deveCriarTrilha() throws Exception {
        TrilhaRequest request =
                criarTrilhaRequest();

        TrilhaResponse response =
                criarTrilhaResponse(
                        1L,
                        "Formação em Java",
                        StatusTrilha.RASCUNHO
                );

        when(trilhaService.criar(
                any(TrilhaRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/trilhas")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/v1/trilhas/1"
                ))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo")
                        .value("Formação em Java"))
                .andExpect(jsonPath("$.descricao")
                        .value(
                                "Trilha completa para aprendizagem de Java."
                        ))
                .andExpect(jsonPath("$.status")
                        .value("RASCUNHO"))
                .andExpect(jsonPath("$.cursos")
                        .isArray());

        verify(trilhaService).criar(
                any(TrilhaRequest.class)
        );
    }

    @Test
    void deveListarTrilhasComFiltrosEPaginacao()
            throws Exception {

        TrilhaResponse trilha1 =
                criarTrilhaResponse(
                        1L,
                        "Formação em Java",
                        StatusTrilha.PUBLICADA
                );

        TrilhaResponse trilha2 =
                criarTrilhaResponse(
                        2L,
                        "Java Web",
                        StatusTrilha.PUBLICADA
                );

        Page<TrilhaResponse> pagina =
                new PageImpl<>(
                        List.of(
                                trilha1,
                                trilha2
                        ),
                        PageRequest.of(0, 10),
                        2
                );

        when(trilhaService.listarTodos(
                eq("Java"),
                eq(StatusTrilha.PUBLICADA),
                any()
        )).thenReturn(pagina);

        mockMvc.perform(
                        get("/api/v1/trilhas")
                                .param(
                                        "titulo",
                                        "Java"
                                )
                                .param(
                                        "status",
                                        "PUBLICADA"
                                )
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .isArray())
                .andExpect(jsonPath("$.content.length()")
                        .value(2))
                .andExpect(jsonPath("$.content[0].id")
                        .value(1))
                .andExpect(jsonPath("$.content[0].titulo")
                        .value("Formação em Java"))
                .andExpect(jsonPath("$.content[1].id")
                        .value(2))
                .andExpect(jsonPath("$.content[1].titulo")
                        .value("Java Web"))
                .andExpect(jsonPath("$.totalElements")
                        .value(2))
                .andExpect(jsonPath("$.number")
                        .value(0))
                .andExpect(jsonPath("$.size")
                        .value(10));

        verify(trilhaService).listarTodos(
                eq("Java"),
                eq(StatusTrilha.PUBLICADA),
                any()
        );
    }

    @Test
    void deveBuscarTrilhaPorId()
            throws Exception {

        TrilhaResponse response =
                criarTrilhaResponse(
                        1L,
                        "Formação em Java",
                        StatusTrilha.PUBLICADA
                );

        response.setCursos(
                List.of(
                        criarTrilhaCursoResponse(
                                10L,
                                2L,
                                1
                        )
                )
        );

        when(trilhaService.buscarPorId(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/trilhas/{id}",
                                1L
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.titulo")
                        .value("Formação em Java"))
                .andExpect(jsonPath("$.status")
                        .value("PUBLICADA"))
                .andExpect(jsonPath("$.cursos.length()")
                        .value(1))
                .andExpect(jsonPath("$.cursos[0].cursoId")
                        .value(2))
                .andExpect(jsonPath("$.cursos[0].ordem")
                        .value(1));

        verify(trilhaService).buscarPorId(1L);
    }

    @Test
    void deveAtualizarTrilha()
            throws Exception {

        TrilhaRequest request =
                criarTrilhaRequest();

        request.setTitulo(
                "Formação Java atualizada"
        );

        request.setStatus(
                StatusTrilha.PUBLICADA
        );

        TrilhaResponse response =
                criarTrilhaResponse(
                        1L,
                        "Formação Java atualizada",
                        StatusTrilha.PUBLICADA
                );

        when(trilhaService.atualizar(
                eq(1L),
                any(TrilhaRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/v1/trilhas/{id}",
                                1L
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.titulo")
                        .value(
                                "Formação Java atualizada"
                        ))
                .andExpect(jsonPath("$.status")
                        .value("PUBLICADA"));

        verify(trilhaService).atualizar(
                eq(1L),
                any(TrilhaRequest.class)
        );
    }

    @Test
    void deveExcluirTrilha()
            throws Exception {

        doNothing()
                .when(trilhaService)
                .excluir(1L);

        mockMvc.perform(
                        delete(
                                "/api/v1/trilhas/{id}",
                                1L
                        )
                )
                .andExpect(status().isNoContent());

        verify(trilhaService).excluir(1L);
    }

    @Test
    void deveAdicionarCursoNaTrilha()
            throws Exception {

        TrilhaCursoRequest request =
                criarTrilhaCursoRequest(
                        2L,
                        1
                );

        TrilhaCursoResponse response =
                criarTrilhaCursoResponse(
                        10L,
                        2L,
                        1
                );

        when(trilhaService.adicionarCurso(
                eq(1L),
                any(TrilhaCursoRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post(
                                "/api/v1/trilhas/{trilhaId}/cursos",
                                1L
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/v1/trilhas/1/cursos/2"
                ))
                .andExpect(jsonPath("$.id")
                        .value(10))
                .andExpect(jsonPath("$.cursoId")
                        .value(2))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Java para iniciantes"))
                .andExpect(jsonPath("$.ordem")
                        .value(1));

        verify(trilhaService).adicionarCurso(
                eq(1L),
                any(TrilhaCursoRequest.class)
        );
    }

    @Test
    void deveAtualizarOrdemDoCurso()
            throws Exception {

        TrilhaCursoOrdemRequest request =
                new TrilhaCursoOrdemRequest();

        request.setOrdem(2);

        TrilhaCursoResponse response =
                criarTrilhaCursoResponse(
                        10L,
                        2L,
                        2
                );

        when(trilhaService.atualizarOrdem(
                eq(1L),
                eq(2L),
                any(TrilhaCursoOrdemRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/trilhas/{trilhaId}"
                                        + "/cursos/{cursoId}/ordem",
                                1L,
                                2L
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(10))
                .andExpect(jsonPath("$.cursoId")
                        .value(2))
                .andExpect(jsonPath("$.ordem")
                        .value(2));

        verify(trilhaService).atualizarOrdem(
                eq(1L),
                eq(2L),
                any(TrilhaCursoOrdemRequest.class)
        );
    }

    @Test
    void deveRemoverCursoDaTrilha()
            throws Exception {

        doNothing()
                .when(trilhaService)
                .removerCurso(
                        1L,
                        2L
                );

        mockMvc.perform(
                        delete(
                                "/api/v1/trilhas/{trilhaId}"
                                        + "/cursos/{cursoId}",
                                1L,
                                2L
                        )
                )
                .andExpect(status().isNoContent());

        verify(trilhaService).removerCurso(
                1L,
                2L
        );
    }

    @Test
    void deveRetornarBadRequestAoCriarTrilhaInvalida()
            throws Exception {

        TrilhaRequest request =
                new TrilhaRequest();

        request.setTitulo("");
        request.setDescricao("");

        mockMvc.perform(
                        post("/api/v1/trilhas")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
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
                        .value("Validation Error"))
                .andExpect(jsonPath("$.errors.titulo")
                        .exists())
                .andExpect(jsonPath("$.errors.descricao")
                        .exists());

        verify(
                trilhaService,
                never()
        ).criar(any(TrilhaRequest.class));
    }

    @Test
    void deveRetornarBadRequestAoAdicionarCursoInvalido()
            throws Exception {

        TrilhaCursoRequest request =
                criarTrilhaCursoRequest(
                        0L,
                        0
                );

        mockMvc.perform(
                        post(
                                "/api/v1/trilhas/{trilhaId}/cursos",
                                1L
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
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
                        .value("Validation Error"))
                .andExpect(jsonPath("$.errors.cursoId")
                        .exists())
                .andExpect(jsonPath("$.errors.ordem")
                        .exists());

        verify(
                trilhaService,
                never()
        ).adicionarCurso(
                eq(1L),
                any(TrilhaCursoRequest.class)
        );
    }

    @Test
    void deveRetornarBadRequestAoAtualizarOrdemInvalida()
            throws Exception {

        TrilhaCursoOrdemRequest request =
                new TrilhaCursoOrdemRequest();

        request.setOrdem(0);

        mockMvc.perform(
                        patch(
                                "/api/v1/trilhas/{trilhaId}"
                                        + "/cursos/{cursoId}/ordem",
                                1L,
                                2L
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
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
                        .value("Validation Error"))
                .andExpect(jsonPath("$.errors.ordem")
                        .exists());

        verify(
                trilhaService,
                never()
        ).atualizarOrdem(
                eq(1L),
                eq(2L),
                any(TrilhaCursoOrdemRequest.class)
        );
    }

    @Test
    void deveRetornarNotFoundAoBuscarTrilhaInexistente()
            throws Exception {

        when(trilhaService.buscarPorId(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Trilha não encontrada com o ID: 99"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/trilhas/{id}",
                                99L
                        )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status")
                        .value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Trilha não encontrada com o ID: 99"
                        ))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/trilhas/99"));

        verify(trilhaService).buscarPorId(99L);
    }

    private TrilhaRequest criarTrilhaRequest() {
        TrilhaRequest request =
                new TrilhaRequest();

        request.setTitulo(
                "Formação em Java"
        );

        request.setDescricao(
                "Trilha completa para aprendizagem de Java."
        );

        request.setStatus(
                StatusTrilha.RASCUNHO
        );

        return request;
    }

    private TrilhaCursoRequest criarTrilhaCursoRequest(
            Long cursoId,
            Integer ordem) {

        TrilhaCursoRequest request =
                new TrilhaCursoRequest();

        request.setCursoId(cursoId);
        request.setOrdem(ordem);

        return request;
    }

    private TrilhaResponse criarTrilhaResponse(
            Long id,
            String titulo,
            StatusTrilha status) {

        TrilhaResponse response =
                new TrilhaResponse();

        response.setId(id);
        response.setTitulo(titulo);
        response.setDescricao(
                "Trilha completa para aprendizagem de Java."
        );
        response.setStatus(status);

        return response;
    }

    private TrilhaCursoResponse criarTrilhaCursoResponse(
            Long id,
            Long cursoId,
            Integer ordem) {

        TrilhaCursoResponse response =
                new TrilhaCursoResponse();

        response.setId(id);
        response.setCursoId(cursoId);
        response.setCursoTitulo(
                "Java para iniciantes"
        );
        response.setOrdem(ordem);

        return response;
    }
}