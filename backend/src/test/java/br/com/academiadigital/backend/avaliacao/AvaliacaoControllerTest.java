package br.com.academiadigital.backend.avaliacao;

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

import java.math.BigDecimal;
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

import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoUpdateRequest;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AvaliacaoController.class)
class AvaliacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AvaliacaoService avaliacaoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void deveCriarAvaliacao() throws Exception {
        AvaliacaoRequest request = criarRequest();

        AvaliacaoResponse response = criarResponse(
                10L,
                "Avaliação de conhecimentos básicos",
                1,
                StatusAvaliacao.RASCUNHO
        );

        when(avaliacaoService.criar(
                any(AvaliacaoRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/avaliacoes")
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
                        "http://localhost/api/v1/avaliacoes/10"
                ))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.cursoId").value(1))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Informática Básica"))
                .andExpect(jsonPath("$.titulo")
                        .value(
                                "Avaliação de conhecimentos básicos"
                        ))
                .andExpect(jsonPath("$.ordem").value(1))
                .andExpect(jsonPath("$.notaMinima")
                        .value(7.0))
                .andExpect(jsonPath("$.maximoTentativas")
                        .value(3))
                .andExpect(jsonPath("$.tempoLimiteMinutos")
                        .value(60))
                .andExpect(jsonPath("$.status")
                        .value("RASCUNHO"));

        verify(avaliacaoService).criar(
                any(AvaliacaoRequest.class)
        );
    }

    @Test
    void deveListarAvaliacoesComFiltrosEPaginacao()
            throws Exception {

        AvaliacaoResponse avaliacao1 =
                criarResponse(
                        10L,
                        "Avaliação inicial",
                        1,
                        StatusAvaliacao.PUBLICADA
                );

        AvaliacaoResponse avaliacao2 =
                criarResponse(
                        11L,
                        "Avaliação final",
                        2,
                        StatusAvaliacao.PUBLICADA
                );

        Page<AvaliacaoResponse> pagina =
                new PageImpl<>(
                        List.of(
                                avaliacao1,
                                avaliacao2
                        ),
                        PageRequest.of(0, 10),
                        2
                );

        when(avaliacaoService.listarTodos(
                eq(1L),
                eq(StatusAvaliacao.PUBLICADA),
                eq("avaliação"),
                any()
        )).thenReturn(pagina);

        mockMvc.perform(
                        get("/api/v1/avaliacoes")
                                .param("cursoId", "1")
                                .param(
                                        "status",
                                        "PUBLICADA"
                                )
                                .param(
                                        "titulo",
                                        "avaliação"
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
                        .value(10))
                .andExpect(jsonPath("$.content[0].titulo")
                        .value("Avaliação inicial"))
                .andExpect(jsonPath("$.content[0].ordem")
                        .value(1))
                .andExpect(jsonPath("$.content[1].id")
                        .value(11))
                .andExpect(jsonPath("$.content[1].titulo")
                        .value("Avaliação final"))
                .andExpect(jsonPath("$.content[1].ordem")
                        .value(2))
                .andExpect(jsonPath("$.totalElements")
                        .value(2))
                .andExpect(jsonPath("$.number")
                        .value(0))
                .andExpect(jsonPath("$.size")
                        .value(10));

        verify(avaliacaoService).listarTodos(
                eq(1L),
                eq(StatusAvaliacao.PUBLICADA),
                eq("avaliação"),
                any()
        );
    }

    @Test
    void deveBuscarAvaliacaoPorId() throws Exception {
        AvaliacaoResponse response =
                criarResponse(
                        10L,
                        "Avaliação de conhecimentos básicos",
                        1,
                        StatusAvaliacao.PUBLICADA
                );

        when(avaliacaoService.buscarPorId(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/avaliacoes/{id}",
                                10L
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(10))
                .andExpect(jsonPath("$.cursoId")
                        .value(1))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Informática Básica"))
                .andExpect(jsonPath("$.titulo")
                        .value(
                                "Avaliação de conhecimentos básicos"
                        ))
                .andExpect(jsonPath("$.descricao")
                        .value(
                                "Avaliação dos conteúdos iniciais do curso."
                        ))
                .andExpect(jsonPath("$.ordem")
                        .value(1))
                .andExpect(jsonPath("$.notaMinima")
                        .value(7.0))
                .andExpect(jsonPath("$.maximoTentativas")
                        .value(3))
                .andExpect(jsonPath("$.tempoLimiteMinutos")
                        .value(60))
                .andExpect(jsonPath("$.status")
                        .value("PUBLICADA"));

        verify(avaliacaoService)
                .buscarPorId(10L);
    }

    @Test
    void deveAtualizarAvaliacao() throws Exception {
        AvaliacaoUpdateRequest request =
                criarUpdateRequest();

        AvaliacaoResponse response =
                criarResponse(
                        10L,
                        "Avaliação atualizada",
                        3,
                        StatusAvaliacao.PUBLICADA
                );

        response.setCursoId(2L);
        response.setCursoTitulo(
                "Curso atualizado"
        );

        response.setDescricao(
                "Descrição atualizada da avaliação."
        );

        response.setNotaMinima(
                new BigDecimal("8.00")
        );

        response.setMaximoTentativas(4);
        response.setTempoLimiteMinutos(90);

        when(avaliacaoService.atualizar(
                eq(10L),
                any(AvaliacaoUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/v1/avaliacoes/{id}",
                                10L
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
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Curso atualizado"))
                .andExpect(jsonPath("$.titulo")
                        .value("Avaliação atualizada"))
                .andExpect(jsonPath("$.descricao")
                        .value(
                                "Descrição atualizada da avaliação."
                        ))
                .andExpect(jsonPath("$.ordem")
                        .value(3))
                .andExpect(jsonPath("$.notaMinima")
                        .value(8.0))
                .andExpect(jsonPath("$.maximoTentativas")
                        .value(4))
                .andExpect(jsonPath("$.tempoLimiteMinutos")
                        .value(90))
                .andExpect(jsonPath("$.status")
                        .value("PUBLICADA"));

        verify(avaliacaoService).atualizar(
                eq(10L),
                any(AvaliacaoUpdateRequest.class)
        );
    }

    @Test
    void deveExcluirAvaliacao() throws Exception {
        doNothing()
                .when(avaliacaoService)
                .excluir(10L);

        mockMvc.perform(
                        delete(
                                "/api/v1/avaliacoes/{id}",
                                10L
                        )
                )
                .andExpect(status().isNoContent());

        verify(avaliacaoService)
                .excluir(10L);
    }

    @Test
    void deveRetornarBadRequestAoCriarComDadosInvalidos()
            throws Exception {

        AvaliacaoRequest request =
                new AvaliacaoRequest();

        request.setCursoId(0L);
        request.setTitulo("");
        request.setDescricao("");
        request.setOrdem(0);

        request.setNotaMinima(
                new BigDecimal("11.00")
        );

        request.setMaximoTentativas(0);
        request.setTempoLimiteMinutos(0);

        mockMvc.perform(
                        post("/api/v1/avaliacoes")
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
                .andExpect(jsonPath("$.errors.titulo")
                        .exists())
                .andExpect(jsonPath("$.errors.descricao")
                        .exists())
                .andExpect(jsonPath("$.errors.ordem")
                        .exists())
                .andExpect(jsonPath("$.errors.notaMinima")
                        .exists())
                .andExpect(
                        jsonPath("$.errors.maximoTentativas")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.errors.tempoLimiteMinutos")
                                .exists()
                );

        verify(
                avaliacaoService,
                never()
        ).criar(any(AvaliacaoRequest.class));
    }

    @Test
    void deveRetornarBadRequestAoAtualizarComDadosInvalidos()
            throws Exception {

        AvaliacaoUpdateRequest request =
                new AvaliacaoUpdateRequest();

        request.setCursoId(0L);
        request.setTitulo("");
        request.setDescricao("");
        request.setOrdem(0);

        request.setNotaMinima(
                new BigDecimal("-1.00")
        );

        request.setMaximoTentativas(0);
        request.setTempoLimiteMinutos(0);

        mockMvc.perform(
                        put(
                                "/api/v1/avaliacoes/{id}",
                                10L
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
                .andExpect(jsonPath("$.errors.titulo")
                        .exists())
                .andExpect(jsonPath("$.errors.descricao")
                        .exists())
                .andExpect(jsonPath("$.errors.ordem")
                        .exists())
                .andExpect(jsonPath("$.errors.notaMinima")
                        .exists())
                .andExpect(
                        jsonPath("$.errors.maximoTentativas")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.errors.tempoLimiteMinutos")
                                .exists()
                );

        verify(
                avaliacaoService,
                never()
        ).atualizar(
                eq(10L),
                any(AvaliacaoUpdateRequest.class)
        );
    }

    @Test
    void deveRetornarNotFoundAoBuscarAvaliacaoInexistente()
            throws Exception {

        when(avaliacaoService.buscarPorId(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Avaliação não encontrada com o ID: 99"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/avaliacoes/{id}",
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
                                "Avaliação não encontrada com o ID: 99"
                        ))
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/v1/avaliacoes/99"
                        ));

        verify(avaliacaoService)
                .buscarPorId(99L);
    }

    private AvaliacaoRequest criarRequest() {
        AvaliacaoRequest request =
                new AvaliacaoRequest();

        request.setCursoId(1L);

        request.setTitulo(
                "Avaliação de conhecimentos básicos"
        );

        request.setDescricao(
                "Avaliação dos conteúdos iniciais do curso."
        );

        request.setOrdem(1);

        request.setNotaMinima(
                new BigDecimal("7.00")
        );

        request.setMaximoTentativas(3);
        request.setTempoLimiteMinutos(60);

        request.setStatus(
                StatusAvaliacao.RASCUNHO
        );

        return request;
    }

    private AvaliacaoUpdateRequest criarUpdateRequest() {
        AvaliacaoUpdateRequest request =
                new AvaliacaoUpdateRequest();

        request.setCursoId(2L);
        request.setTitulo(
                "Avaliação atualizada"
        );

        request.setDescricao(
                "Descrição atualizada da avaliação."
        );

        request.setOrdem(3);

        request.setNotaMinima(
                new BigDecimal("8.00")
        );

        request.setMaximoTentativas(4);
        request.setTempoLimiteMinutos(90);

        request.setStatus(
                StatusAvaliacao.PUBLICADA
        );

        return request;
    }

    private AvaliacaoResponse criarResponse(
            Long id,
            String titulo,
            Integer ordem,
            StatusAvaliacao status) {

        AvaliacaoResponse response =
                new AvaliacaoResponse();

        response.setId(id);
        response.setCursoId(1L);

        response.setCursoTitulo(
                "Informática Básica"
        );

        response.setTitulo(titulo);

        response.setDescricao(
                "Avaliação dos conteúdos iniciais do curso."
        );

        response.setOrdem(ordem);

        response.setNotaMinima(
                new BigDecimal("7.00")
        );

        response.setMaximoTentativas(3);
        response.setTempoLimiteMinutos(60);
        response.setStatus(status);

        return response;
    }
}
