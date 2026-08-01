package br.com.academiadigital.backend.aula;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import br.com.academiadigital.backend.aula.dto.AulaRequest;
import br.com.academiadigital.backend.aula.dto.AulaResponse;
import br.com.academiadigital.backend.aula.dto.AulaUpdateRequest;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AulaController.class)
class AulaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AulaService aulaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void deveCriarAula() throws Exception {
        AulaRequest request = criarRequest();

        AulaResponse response = criarResponse(
                10L,
                "Introdução ao computador",
                1,
                StatusAula.RASCUNHO
        );

        when(aulaService.criar(any(AulaRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/aulas")
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
                        "http://localhost/api/v1/aulas/10"
                ))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.cursoId").value(1))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Informática Básica"))
                .andExpect(jsonPath("$.titulo")
                        .value("Introdução ao computador"))
                .andExpect(jsonPath("$.ordem").value(1))
                .andExpect(jsonPath("$.duracaoMinutos").value(30))
                .andExpect(jsonPath("$.status")
                        .value("RASCUNHO"));

        verify(aulaService).criar(
                any(AulaRequest.class)
        );
    }

    @Test
    void deveListarAulasComFiltrosEPaginacao()
            throws Exception {

        AulaResponse aula1 = criarResponse(
                10L,
                "Introdução ao computador",
                1,
                StatusAula.PUBLICADA
        );

        AulaResponse aula2 = criarResponse(
                11L,
                "Componentes do computador",
                2,
                StatusAula.PUBLICADA
        );

        Page<AulaResponse> pagina = new PageImpl<>(
                List.of(aula1, aula2),
                PageRequest.of(0, 10),
                2
        );

        when(aulaService.listarTodos(
                eq(1L),
                eq(StatusAula.PUBLICADA),
                eq("computador"),
                any()
        )).thenReturn(pagina);

        mockMvc.perform(
                        get("/api/v1/aulas")
                                .param("cursoId", "1")
                                .param("status", "PUBLICADA")
                                .param("titulo", "computador")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()")
                        .value(2))
                .andExpect(jsonPath("$.content[0].id")
                        .value(10))
                .andExpect(jsonPath("$.content[0].titulo")
                        .value("Introdução ao computador"))
                .andExpect(jsonPath("$.content[0].ordem")
                        .value(1))
                .andExpect(jsonPath("$.content[1].id")
                        .value(11))
                .andExpect(jsonPath("$.content[1].titulo")
                        .value("Componentes do computador"))
                .andExpect(jsonPath("$.content[1].ordem")
                        .value(2))
                .andExpect(jsonPath("$.totalElements")
                        .value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(aulaService).listarTodos(
                eq(1L),
                eq(StatusAula.PUBLICADA),
                eq("computador"),
                any()
        );
    }

    @Test
    void deveBuscarAulaPorId() throws Exception {
        AulaResponse response = criarResponse(
                10L,
                "Introdução ao computador",
                1,
                StatusAula.PUBLICADA
        );

        when(aulaService.buscarPorId(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/aulas/{id}", 10L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.cursoId").value(1))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Informática Básica"))
                .andExpect(jsonPath("$.titulo")
                        .value("Introdução ao computador"))
                .andExpect(jsonPath("$.descricao")
                        .value(
                                "Conhecendo os componentes básicos do computador."
                        ))
                .andExpect(jsonPath("$.ordem").value(1))
                .andExpect(jsonPath("$.duracaoMinutos")
                        .value(30))
                .andExpect(jsonPath("$.videoUrl")
                        .value(
                                "https://exemplo.com/videos/aula-1"
                        ))
                .andExpect(jsonPath("$.status")
                        .value("PUBLICADA"));

        verify(aulaService).buscarPorId(10L);
    }

    @Test
    void deveAtualizarAula() throws Exception {
        AulaUpdateRequest request =
                criarUpdateRequest();

        AulaResponse response = criarResponse(
                10L,
                "Aula atualizada",
                3,
                StatusAula.PUBLICADA
        );

        response.setCursoId(2L);
        response.setCursoTitulo("Curso atualizado");
        response.setDescricao(
                "Descrição atualizada da aula."
        );
        response.setDuracaoMinutos(45);
        response.setVideoUrl(
                "https://exemplo.com/videos/aula-atualizada"
        );

        when(aulaService.atualizar(
                eq(10L),
                any(AulaUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/aulas/{id}", 10L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.cursoId").value(2))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Curso atualizado"))
                .andExpect(jsonPath("$.titulo")
                        .value("Aula atualizada"))
                .andExpect(jsonPath("$.descricao")
                        .value(
                                "Descrição atualizada da aula."
                        ))
                .andExpect(jsonPath("$.ordem").value(3))
                .andExpect(jsonPath("$.duracaoMinutos")
                        .value(45))
                .andExpect(jsonPath("$.status")
                        .value("PUBLICADA"));

        verify(aulaService).atualizar(
                eq(10L),
                any(AulaUpdateRequest.class)
        );
    }

    @Test
    void deveExcluirAula() throws Exception {
        doNothing()
                .when(aulaService)
                .excluir(10L);

        mockMvc.perform(
                        delete("/api/v1/aulas/{id}", 10L)
                )
                .andExpect(status().isNoContent());

        verify(aulaService).excluir(10L);
    }

    @Test
    void deveRetornarBadRequestAoCriarAulaComDadosInvalidos()
            throws Exception {

        AulaRequest request = new AulaRequest();

        request.setCursoId(0L);
        request.setTitulo("");
        request.setDescricao("");
        request.setOrdem(0);
        request.setDuracaoMinutos(0);

        mockMvc.perform(
                        post("/api/v1/aulas")
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
                .andExpect(jsonPath("$.errors.cursoId")
                        .exists())
                .andExpect(jsonPath("$.errors.titulo")
                        .exists())
                .andExpect(jsonPath("$.errors.descricao")
                        .exists())
                .andExpect(jsonPath("$.errors.ordem")
                        .exists())
                .andExpect(
                        jsonPath("$.errors.duracaoMinutos")
                                .exists()
                );

        verify(
                aulaService,
                never()
        ).criar(any(AulaRequest.class));
    }

    @Test
    void deveRetornarBadRequestAoAtualizarComDadosInvalidos()
            throws Exception {

        AulaUpdateRequest request =
                new AulaUpdateRequest();

        request.setCursoId(0L);
        request.setTitulo("");
        request.setDescricao("");
        request.setOrdem(0);
        request.setDuracaoMinutos(0);

        mockMvc.perform(
                        put("/api/v1/aulas/{id}", 10L)
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
                .andExpect(jsonPath("$.errors.cursoId")
                        .exists())
                .andExpect(jsonPath("$.errors.titulo")
                        .exists())
                .andExpect(jsonPath("$.errors.descricao")
                        .exists())
                .andExpect(jsonPath("$.errors.ordem")
                        .exists())
                .andExpect(
                        jsonPath("$.errors.duracaoMinutos")
                                .exists()
                );

        verify(
                aulaService,
                never()
        ).atualizar(
                eq(10L),
                any(AulaUpdateRequest.class)
        );
    }

    @Test
    void deveRetornarNotFoundAoBuscarAulaInexistente()
            throws Exception {

        when(aulaService.buscarPorId(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Aula não encontrada com o ID: 99"
                        )
                );

        mockMvc.perform(
                        get("/api/v1/aulas/{id}", 99L)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Aula não encontrada com o ID: 99"
                        ))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/aulas/99"));

        verify(aulaService).buscarPorId(99L);
    }

    private AulaRequest criarRequest() {
        AulaRequest request = new AulaRequest();

        request.setCursoId(1L);
        request.setTitulo(
                "Introdução ao computador"
        );
        request.setDescricao(
                "Conhecendo os componentes básicos do computador."
        );
        request.setOrdem(1);
        request.setDuracaoMinutos(30);
        request.setVideoUrl(
                "https://exemplo.com/videos/aula-1"
        );
        request.setStatus(StatusAula.RASCUNHO);

        return request;
    }

    private AulaUpdateRequest criarUpdateRequest() {
        AulaUpdateRequest request =
                new AulaUpdateRequest();

        request.setCursoId(2L);
        request.setTitulo("Aula atualizada");
        request.setDescricao(
                "Descrição atualizada da aula."
        );
        request.setOrdem(3);
        request.setDuracaoMinutos(45);
        request.setVideoUrl(
                "https://exemplo.com/videos/aula-atualizada"
        );
        request.setStatus(StatusAula.PUBLICADA);

        return request;
    }

    private AulaResponse criarResponse(
            Long id,
            String titulo,
            Integer ordem,
            StatusAula status) {

        AulaResponse response = new AulaResponse();

        response.setId(id);
        response.setCursoId(1L);
        response.setCursoTitulo("Informática Básica");
        response.setTitulo(titulo);
        response.setDescricao(
                "Conhecendo os componentes básicos do computador."
        );
        response.setOrdem(ordem);
        response.setDuracaoMinutos(30);
        response.setVideoUrl(
                "https://exemplo.com/videos/aula-1"
        );
        response.setStatus(status);

        return response;
    }
}