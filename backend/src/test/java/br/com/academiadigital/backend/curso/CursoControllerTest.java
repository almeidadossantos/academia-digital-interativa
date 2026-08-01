package br.com.academiadigital.backend.curso;

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

import br.com.academiadigital.backend.curso.dto.CursoRequest;
import br.com.academiadigital.backend.curso.dto.CursoResponse;
import br.com.academiadigital.backend.curso.dto.CursoUpdateRequest;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CursoController.class)
class CursoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CursoService cursoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void deveCriarCurso() throws Exception {
        CursoRequest request = criarRequest();
        CursoResponse response = criarResponse(
                1L,
                "Java para iniciantes",
                NivelCurso.INICIANTE,
                StatusCurso.RASCUNHO
        );

        when(cursoService.criar(any(CursoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/cursos")
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
                        "http://localhost/api/v1/cursos/1"
                ))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo")
                        .value("Java para iniciantes"))
                .andExpect(jsonPath("$.cargaHoraria").value(40))
                .andExpect(jsonPath("$.nivel").value("INICIANTE"))
                .andExpect(jsonPath("$.status").value("RASCUNHO"));

        verify(cursoService).criar(any(CursoRequest.class));
    }

    @Test
    void deveListarCursosComFiltrosEPaginacao()
            throws Exception {

        CursoResponse curso1 = criarResponse(
                1L,
                "Java para iniciantes",
                NivelCurso.INICIANTE,
                StatusCurso.PUBLICADO
        );

        CursoResponse curso2 = criarResponse(
                2L,
                "Java com Spring Boot",
                NivelCurso.INTERMEDIARIO,
                StatusCurso.PUBLICADO
        );

        Page<CursoResponse> pagina = new PageImpl<>(
                List.of(curso1, curso2),
                PageRequest.of(0, 10),
                2
        );

        when(cursoService.listarTodos(
                eq("java"),
                eq(NivelCurso.INICIANTE),
                eq(StatusCurso.PUBLICADO),
                any()
        )).thenReturn(pagina);

        mockMvc.perform(
                        get("/api/v1/cursos")
                                .param("titulo", "java")
                                .param("nivel", "INICIANTE")
                                .param("status", "PUBLICADO")
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].titulo")
                        .value("Java para iniciantes"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].titulo")
                        .value("Java com Spring Boot"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(10));

        verify(cursoService).listarTodos(
                eq("java"),
                eq(NivelCurso.INICIANTE),
                eq(StatusCurso.PUBLICADO),
                any()
        );
    }

    @Test
    void deveBuscarCursoPorId() throws Exception {
        CursoResponse response = criarResponse(
                1L,
                "Java para iniciantes",
                NivelCurso.INICIANTE,
                StatusCurso.PUBLICADO
        );

        when(cursoService.buscarPorId(1L))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/cursos/{id}", 1L)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo")
                        .value("Java para iniciantes"))
                .andExpect(jsonPath("$.descricao")
                        .value("Curso introdutório de Java."))
                .andExpect(jsonPath("$.cargaHoraria").value(40))
                .andExpect(jsonPath("$.nivel").value("INICIANTE"))
                .andExpect(jsonPath("$.status").value("PUBLICADO"));

        verify(cursoService).buscarPorId(1L);
    }

    @Test
    void deveAtualizarCurso() throws Exception {
        CursoUpdateRequest request = criarUpdateRequest();

        CursoResponse response = criarResponse(
                1L,
                "Spring Boot avançado",
                NivelCurso.AVANCADO,
                StatusCurso.PUBLICADO
        );

        response.setDescricao("Curso atualizado.");
        response.setCargaHoraria(80);

        when(cursoService.atualizar(
                eq(1L),
                any(CursoUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/cursos/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo")
                        .value("Spring Boot avançado"))
                .andExpect(jsonPath("$.descricao")
                        .value("Curso atualizado."))
                .andExpect(jsonPath("$.cargaHoraria").value(80))
                .andExpect(jsonPath("$.nivel").value("AVANCADO"))
                .andExpect(jsonPath("$.status").value("PUBLICADO"));

        verify(cursoService).atualizar(
                eq(1L),
                any(CursoUpdateRequest.class)
        );
    }

    @Test
    void deveExcluirCurso() throws Exception {
        doNothing()
                .when(cursoService)
                .excluir(1L);

        mockMvc.perform(
                        delete("/api/v1/cursos/{id}", 1L)
                )
                .andExpect(status().isNoContent());

        verify(cursoService).excluir(1L);
    }

    @Test
    void deveRetornarBadRequestAoCriarCursoComDadosInvalidos()
            throws Exception {

        CursoRequest request = new CursoRequest();
        request.setTitulo("");
        request.setDescricao("");
        request.setCargaHoraria(0);
        request.setNivel(null);

        mockMvc.perform(
                        post("/api/v1/cursos")
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
                .andExpect(jsonPath("$.errors.titulo").exists())
                .andExpect(jsonPath("$.errors.descricao").exists())
                .andExpect(jsonPath("$.errors.cargaHoraria").exists())
                .andExpect(jsonPath("$.errors.nivel").exists());

        verify(cursoService, never())
                .criar(any(CursoRequest.class));
    }

    @Test
    void deveRetornarBadRequestAoAtualizarComDadosInvalidos()
            throws Exception {

        CursoUpdateRequest request = new CursoUpdateRequest();
        request.setTitulo("");
        request.setDescricao("");
        request.setCargaHoraria(0);
        request.setNivel(null);

        mockMvc.perform(
                        put("/api/v1/cursos/{id}", 1L)
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
                        .value("Validation Error"));

        verify(cursoService, never()).atualizar(
                eq(1L),
                any(CursoUpdateRequest.class)
        );
    }

    @Test
    void deveRetornarNotFoundAoBuscarCursoInexistente()
            throws Exception {

        when(cursoService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException(
                        "Curso não encontrado com o ID: 99"
                ));

        mockMvc.perform(
                        get("/api/v1/cursos/{id}", 99L)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Curso não encontrado com o ID: 99"))
                .andExpect(jsonPath("$.path")
                        .value("/api/v1/cursos/99"));

        verify(cursoService).buscarPorId(99L);
    }

    private CursoRequest criarRequest() {
        CursoRequest request = new CursoRequest();
        request.setTitulo("Java para iniciantes");
        request.setDescricao("Curso introdutório de Java.");
        request.setCargaHoraria(40);
        request.setNivel(NivelCurso.INICIANTE);
        request.setStatus(StatusCurso.RASCUNHO);
        request.setImagemUrl(
                "https://exemplo.com/java.png"
        );
        return request;
    }

    private CursoUpdateRequest criarUpdateRequest() {
        CursoUpdateRequest request = new CursoUpdateRequest();
        request.setTitulo("Spring Boot avançado");
        request.setDescricao("Curso atualizado.");
        request.setCargaHoraria(80);
        request.setNivel(NivelCurso.AVANCADO);
        request.setStatus(StatusCurso.PUBLICADO);
        request.setImagemUrl(
                "https://exemplo.com/spring.png"
        );
        return request;
    }

    private CursoResponse criarResponse(
            Long id,
            String titulo,
            NivelCurso nivel,
            StatusCurso status) {

        CursoResponse response = new CursoResponse();
        response.setId(id);
        response.setTitulo(titulo);
        response.setDescricao("Curso introdutório de Java.");
        response.setCargaHoraria(40);
        response.setNivel(nivel);
        response.setStatus(status);
        response.setImagemUrl(
                "https://exemplo.com/curso.png"
        );
        return response;
    }
}