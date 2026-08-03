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

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.academiadigital.backend.avaliacao.dto.AlternativaRequest;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaResponse;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaUpdateRequest;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AlternativaController.class)
class AlternativaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlternativaService alternativaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void deveCriarAlternativa() throws Exception {
        AlternativaRequest request =
                criarRequest();

        AlternativaResponse response =
                criarResponse(
                        10L,
                        1L,
                        "Processador",
                        true,
                        1
                );

        when(alternativaService.criar(
                any(AlternativaRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/alternativas")
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
                        "http://localhost/api/v1/alternativas/10"
                ))
                .andExpect(jsonPath("$.id")
                        .value(10))
                .andExpect(jsonPath("$.questaoId")
                        .value(1))
                .andExpect(jsonPath("$.questaoEnunciado")
                        .value(
                                "Qual componente executa as instruções?"
                        ))
                .andExpect(jsonPath("$.texto")
                        .value("Processador"))
                .andExpect(jsonPath("$.correta")
                        .value(true))
                .andExpect(jsonPath("$.ordem")
                        .value(1));

        verify(alternativaService).criar(
                any(AlternativaRequest.class)
        );
    }

    @Test
    void deveListarAlternativasPorQuestao()
            throws Exception {

        AlternativaResponse primeiraAlternativa =
                criarResponse(
                        10L,
                        1L,
                        "Processador",
                        true,
                        1
                );

        AlternativaResponse segundaAlternativa =
                criarResponse(
                        11L,
                        1L,
                        "Memória RAM",
                        false,
                        2
                );

        when(alternativaService
                .listarPorQuestao(1L))
                .thenReturn(
                        List.of(
                                primeiraAlternativa,
                                segundaAlternativa
                        )
                );

        mockMvc.perform(
                        get("/api/v1/alternativas")
                                .param(
                                        "questaoId",
                                        "1"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$")
                        .isArray())
                .andExpect(jsonPath("$.length()")
                        .value(2))
                .andExpect(jsonPath("$[0].id")
                        .value(10))
                .andExpect(jsonPath("$[0].texto")
                        .value("Processador"))
                .andExpect(jsonPath("$[0].correta")
                        .value(true))
                .andExpect(jsonPath("$[0].ordem")
                        .value(1))
                .andExpect(jsonPath("$[1].id")
                        .value(11))
                .andExpect(jsonPath("$[1].texto")
                        .value("Memória RAM"))
                .andExpect(jsonPath("$[1].correta")
                        .value(false))
                .andExpect(jsonPath("$[1].ordem")
                        .value(2));

        verify(alternativaService)
                .listarPorQuestao(1L);
    }

    @Test
    void deveBuscarAlternativaPorId()
            throws Exception {

        AlternativaResponse response =
                criarResponse(
                        10L,
                        1L,
                        "Processador",
                        true,
                        1
                );

        when(alternativaService.buscarPorId(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/alternativas/{id}",
                                10L
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(10))
                .andExpect(jsonPath("$.questaoId")
                        .value(1))
                .andExpect(jsonPath("$.questaoEnunciado")
                        .value(
                                "Qual componente executa as instruções?"
                        ))
                .andExpect(jsonPath("$.texto")
                        .value("Processador"))
                .andExpect(jsonPath("$.correta")
                        .value(true))
                .andExpect(jsonPath("$.ordem")
                        .value(1));

        verify(alternativaService)
                .buscarPorId(10L);
    }

    @Test
    void deveAtualizarAlternativa()
            throws Exception {

        AlternativaUpdateRequest request =
                criarUpdateRequest();

        AlternativaResponse response =
                criarResponse(
                        10L,
                        2L,
                        "Memória RAM",
                        false,
                        3
                );

        response.setQuestaoEnunciado(
                "Qual componente armazena dados temporariamente?"
        );

        when(alternativaService.atualizar(
                eq(10L),
                any(AlternativaUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/v1/alternativas/{id}",
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
                .andExpect(jsonPath("$.questaoId")
                        .value(2))
                .andExpect(jsonPath("$.questaoEnunciado")
                        .value(
                                "Qual componente armazena dados temporariamente?"
                        ))
                .andExpect(jsonPath("$.texto")
                        .value("Memória RAM"))
                .andExpect(jsonPath("$.correta")
                        .value(false))
                .andExpect(jsonPath("$.ordem")
                        .value(3));

        verify(alternativaService).atualizar(
                eq(10L),
                any(AlternativaUpdateRequest.class)
        );
    }

    @Test
    void deveExcluirAlternativa()
            throws Exception {

        doNothing()
                .when(alternativaService)
                .excluir(10L);

        mockMvc.perform(
                        delete(
                                "/api/v1/alternativas/{id}",
                                10L
                        )
                )
                .andExpect(status().isNoContent());

        verify(alternativaService)
                .excluir(10L);
    }

    @Test
    void deveRetornarBadRequestAoCriarComDadosInvalidos()
            throws Exception {

        AlternativaRequest request =
                new AlternativaRequest();

        request.setQuestaoId(0L);
        request.setTexto("");
        request.setCorreta(null);
        request.setOrdem(0);

        mockMvc.perform(
                        post("/api/v1/alternativas")
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
                .andExpect(jsonPath("$.errors.questaoId")
                        .exists())
                .andExpect(jsonPath("$.errors.texto")
                        .exists())
                .andExpect(jsonPath("$.errors.correta")
                        .exists())
                .andExpect(jsonPath("$.errors.ordem")
                        .exists());

        verify(
                alternativaService,
                never()
        ).criar(any(AlternativaRequest.class));
    }

    @Test
    void deveRetornarBadRequestAoAtualizarComDadosInvalidos()
            throws Exception {

        AlternativaUpdateRequest request =
                new AlternativaUpdateRequest();

        request.setQuestaoId(0L);
        request.setTexto("");
        request.setCorreta(null);
        request.setOrdem(0);

        mockMvc.perform(
                        put(
                                "/api/v1/alternativas/{id}",
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
                .andExpect(jsonPath("$.errors.questaoId")
                        .exists())
                .andExpect(jsonPath("$.errors.texto")
                        .exists())
                .andExpect(jsonPath("$.errors.correta")
                        .exists())
                .andExpect(jsonPath("$.errors.ordem")
                        .exists());

        verify(
                alternativaService,
                never()
        ).atualizar(
                eq(10L),
                any(AlternativaUpdateRequest.class)
        );
    }

    @Test
    void deveRetornarNotFoundAoBuscarAlternativaInexistente()
            throws Exception {

        when(alternativaService.buscarPorId(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Alternativa não encontrada com o ID: 99"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/alternativas/{id}",
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
                                "Alternativa não encontrada com o ID: 99"
                        ))
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/v1/alternativas/99"
                        ));

        verify(alternativaService)
                .buscarPorId(99L);
    }

    private AlternativaRequest criarRequest() {
        AlternativaRequest request =
                new AlternativaRequest();

        request.setQuestaoId(1L);
        request.setTexto("Processador");
        request.setCorreta(true);
        request.setOrdem(1);

        return request;
    }

    private AlternativaUpdateRequest criarUpdateRequest() {
        AlternativaUpdateRequest request =
                new AlternativaUpdateRequest();

        request.setQuestaoId(2L);
        request.setTexto("Memória RAM");
        request.setCorreta(false);
        request.setOrdem(3);

        return request;
    }

    private AlternativaResponse criarResponse(
            Long id,
            Long questaoId,
            String texto,
            Boolean correta,
            Integer ordem) {

        AlternativaResponse response =
                new AlternativaResponse();

        response.setId(id);
        response.setQuestaoId(questaoId);

        response.setQuestaoEnunciado(
                "Qual componente executa as instruções?"
        );

        response.setTexto(texto);
        response.setCorreta(correta);
        response.setOrdem(ordem);

        return response;
    }
}
