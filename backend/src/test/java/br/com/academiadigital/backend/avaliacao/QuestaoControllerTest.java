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

import br.com.academiadigital.backend.avaliacao.dto.QuestaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoUpdateRequest;
import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(QuestaoController.class)
class QuestaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QuestaoService questaoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void deveCriarQuestao() throws Exception {
        QuestaoRequest request = criarRequest();

        QuestaoResponse response =
                criarResponse(
                        10L,
                        "Qual componente executa as instruções?",
                        1,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        when(questaoService.criar(
                any(QuestaoRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/questoes")
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
                        "http://localhost/api/v1/questoes/10"
                ))
                .andExpect(jsonPath("$.id")
                        .value(10))
                .andExpect(jsonPath("$.avaliacaoId")
                        .value(1))
                .andExpect(jsonPath("$.avaliacaoTitulo")
                        .value("Avaliação inicial"))
                .andExpect(jsonPath("$.enunciado")
                        .value(
                                "Qual componente executa as instruções?"
                        ))
                .andExpect(jsonPath("$.tipo")
                        .value("MULTIPLA_ESCOLHA"))
                .andExpect(jsonPath("$.ordem")
                        .value(1))
                .andExpect(jsonPath("$.pontuacao")
                        .value(2.5));

        verify(questaoService).criar(
                any(QuestaoRequest.class)
        );
    }

    @Test
    void deveListarQuestoesComFiltrosEPaginacao()
            throws Exception {

        QuestaoResponse questao1 =
                criarResponse(
                        10L,
                        "Primeira questão",
                        1,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        QuestaoResponse questao2 =
                criarResponse(
                        11L,
                        "Segunda questão",
                        2,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        Page<QuestaoResponse> pagina =
                new PageImpl<>(
                        List.of(
                                questao1,
                                questao2
                        ),
                        PageRequest.of(0, 10),
                        2
                );

        when(questaoService.listarTodos(
                eq(1L),
                eq(TipoQuestao.MULTIPLA_ESCOLHA),
                eq("componente"),
                any()
        )).thenReturn(pagina);

        mockMvc.perform(
                        get("/api/v1/questoes")
                                .param(
                                        "avaliacaoId",
                                        "1"
                                )
                                .param(
                                        "tipo",
                                        "MULTIPLA_ESCOLHA"
                                )
                                .param(
                                        "enunciado",
                                        "componente"
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
                .andExpect(jsonPath("$.content[0].enunciado")
                        .value("Primeira questão"))
                .andExpect(jsonPath("$.content[0].ordem")
                        .value(1))
                .andExpect(jsonPath("$.content[1].id")
                        .value(11))
                .andExpect(jsonPath("$.content[1].enunciado")
                        .value("Segunda questão"))
                .andExpect(jsonPath("$.content[1].ordem")
                        .value(2))
                .andExpect(jsonPath("$.totalElements")
                        .value(2))
                .andExpect(jsonPath("$.number")
                        .value(0))
                .andExpect(jsonPath("$.size")
                        .value(10));

        verify(questaoService).listarTodos(
                eq(1L),
                eq(TipoQuestao.MULTIPLA_ESCOLHA),
                eq("componente"),
                any()
        );
    }

    @Test
    void deveBuscarQuestaoPorId() throws Exception {
        QuestaoResponse response =
                criarResponse(
                        10L,
                        "Qual componente executa as instruções?",
                        1,
                        TipoQuestao.MULTIPLA_ESCOLHA
                );

        when(questaoService.buscarPorId(10L))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/questoes/{id}",
                                10L
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(10))
                .andExpect(jsonPath("$.avaliacaoId")
                        .value(1))
                .andExpect(jsonPath("$.avaliacaoTitulo")
                        .value("Avaliação inicial"))
                .andExpect(jsonPath("$.enunciado")
                        .value(
                                "Qual componente executa as instruções?"
                        ))
                .andExpect(jsonPath("$.tipo")
                        .value("MULTIPLA_ESCOLHA"))
                .andExpect(jsonPath("$.ordem")
                        .value(1))
                .andExpect(jsonPath("$.pontuacao")
                        .value(2.5));

        verify(questaoService)
                .buscarPorId(10L);
    }

    @Test
    void deveAtualizarQuestao() throws Exception {
        QuestaoUpdateRequest request =
                criarUpdateRequest();

        QuestaoResponse response =
                criarResponse(
                        10L,
                        "Explique o funcionamento do processador.",
                        3,
                        TipoQuestao.DISSERTATIVA
                );

        response.setAvaliacaoId(2L);
        response.setAvaliacaoTitulo(
                "Avaliação atualizada"
        );
        response.setPontuacao(
                new BigDecimal("4.00")
        );

        when(questaoService.atualizar(
                eq(10L),
                any(QuestaoUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/v1/questoes/{id}",
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
                .andExpect(jsonPath("$.avaliacaoId")
                        .value(2))
                .andExpect(jsonPath("$.avaliacaoTitulo")
                        .value("Avaliação atualizada"))
                .andExpect(jsonPath("$.enunciado")
                        .value(
                                "Explique o funcionamento do processador."
                        ))
                .andExpect(jsonPath("$.tipo")
                        .value("DISSERTATIVA"))
                .andExpect(jsonPath("$.ordem")
                        .value(3))
                .andExpect(jsonPath("$.pontuacao")
                        .value(4.0));

        verify(questaoService).atualizar(
                eq(10L),
                any(QuestaoUpdateRequest.class)
        );
    }

    @Test
    void deveExcluirQuestao() throws Exception {
        doNothing()
                .when(questaoService)
                .excluir(10L);

        mockMvc.perform(
                        delete(
                                "/api/v1/questoes/{id}",
                                10L
                        )
                )
                .andExpect(status().isNoContent());

        verify(questaoService)
                .excluir(10L);
    }

    @Test
    void deveRetornarBadRequestAoCriarComDadosInvalidos()
            throws Exception {

        QuestaoRequest request =
                new QuestaoRequest();

        request.setAvaliacaoId(0L);
        request.setEnunciado("");
        request.setTipo(null);
        request.setOrdem(0);

        request.setPontuacao(
                new BigDecimal("11.00")
        );

        mockMvc.perform(
                        post("/api/v1/questoes")
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
                .andExpect(jsonPath("$.errors.avaliacaoId")
                        .exists())
                .andExpect(jsonPath("$.errors.enunciado")
                        .exists())
                .andExpect(jsonPath("$.errors.tipo")
                        .exists())
                .andExpect(jsonPath("$.errors.ordem")
                        .exists())
                .andExpect(jsonPath("$.errors.pontuacao")
                        .exists());

        verify(
                questaoService,
                never()
        ).criar(any(QuestaoRequest.class));
    }

    @Test
    void deveRetornarBadRequestAoAtualizarComDadosInvalidos()
            throws Exception {

        QuestaoUpdateRequest request =
                new QuestaoUpdateRequest();

        request.setAvaliacaoId(0L);
        request.setEnunciado("");
        request.setTipo(null);
        request.setOrdem(0);

        request.setPontuacao(
                new BigDecimal("0.00")
        );

        mockMvc.perform(
                        put(
                                "/api/v1/questoes/{id}",
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
                .andExpect(jsonPath("$.errors.avaliacaoId")
                        .exists())
                .andExpect(jsonPath("$.errors.enunciado")
                        .exists())
                .andExpect(jsonPath("$.errors.tipo")
                        .exists())
                .andExpect(jsonPath("$.errors.ordem")
                        .exists())
                .andExpect(jsonPath("$.errors.pontuacao")
                        .exists());

        verify(
                questaoService,
                never()
        ).atualizar(
                eq(10L),
                any(QuestaoUpdateRequest.class)
        );
    }

    @Test
    void deveRetornarNotFoundAoBuscarQuestaoInexistente()
            throws Exception {

        when(questaoService.buscarPorId(99L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Questão não encontrada com o ID: 99"
                        )
                );

        mockMvc.perform(
                        get(
                                "/api/v1/questoes/{id}",
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
                                "Questão não encontrada com o ID: 99"
                        ))
                .andExpect(jsonPath("$.path")
                        .value(
                                "/api/v1/questoes/99"
                        ));

        verify(questaoService)
                .buscarPorId(99L);
    }

    private QuestaoRequest criarRequest() {
        QuestaoRequest request =
                new QuestaoRequest();

        request.setAvaliacaoId(1L);

        request.setEnunciado(
                "Qual componente executa as instruções?"
        );

        request.setTipo(
                TipoQuestao.MULTIPLA_ESCOLHA
        );

        request.setOrdem(1);

        request.setPontuacao(
                new BigDecimal("2.50")
        );

        return request;
    }

    private QuestaoUpdateRequest criarUpdateRequest() {
        QuestaoUpdateRequest request =
                new QuestaoUpdateRequest();

        request.setAvaliacaoId(2L);

        request.setEnunciado(
                "Explique o funcionamento do processador."
        );

        request.setTipo(
                TipoQuestao.DISSERTATIVA
        );

        request.setOrdem(3);

        request.setPontuacao(
                new BigDecimal("4.00")
        );

        return request;
    }

    private QuestaoResponse criarResponse(
            Long id,
            String enunciado,
            Integer ordem,
            TipoQuestao tipo) {

        QuestaoResponse response =
                new QuestaoResponse();

        response.setId(id);
        response.setAvaliacaoId(1L);

        response.setAvaliacaoTitulo(
                "Avaliação inicial"
        );

        response.setEnunciado(enunciado);
        response.setTipo(tipo);
        response.setOrdem(ordem);

        response.setPontuacao(
                new BigDecimal("2.50")
        );

        return response;
    }
}
