package br.com.academiadigital.backend.tentativa;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.security.Principal;
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

import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtService;
import br.com.academiadigital.backend.tentativa.dto.CorrecaoRespostaRequest;
import br.com.academiadigital.backend.tentativa.dto.RespostaQuestaoRequest;
import br.com.academiadigital.backend.tentativa.dto.RespostaQuestaoResponse;
import br.com.academiadigital.backend.tentativa.dto.TentativaAvaliacaoResponse;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TentativaAvaliacaoController.class)
class TentativaAvaliacaoControllerTest {

    private static final String EMAIL = "aluno@email.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TentativaAvaliacaoService tentativaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void deveIniciarTentativa() throws Exception {
        TentativaAvaliacaoResponse response =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.EM_ANDAMENTO
                );

        when(tentativaService.iniciar(EMAIL, 10L))
                .thenReturn(response);

        mockMvc.perform(
                        post(
                                "/api/v1/tentativas-avaliacao/avaliacoes/10/iniciar"
                        )
                                .principal(principal())
                )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/v1/tentativas-avaliacao/100"
                ))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status")
                        .value("EM_ANDAMENTO"));

        verify(tentativaService).iniciar(EMAIL, 10L);
    }

    @Test
    void deveSalvarResposta() throws Exception {
        RespostaQuestaoRequest request =
                new RespostaQuestaoRequest();
        request.setAlternativaId(30L);

        RespostaQuestaoResponse response =
                new RespostaQuestaoResponse();
        response.setId(40L);
        response.setQuestaoId(20L);
        response.setAlternativaSelecionadaId(30L);
        response.setCorrigida(false);
        response.setPontuacaoObtida(BigDecimal.ZERO);

        when(tentativaService.salvarResposta(
                eq(EMAIL),
                eq(100L),
                eq(20L),
                any(RespostaQuestaoRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/v1/tentativas-avaliacao/100/respostas/20"
                        )
                                .principal(principal())
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
                .andExpect(jsonPath("$.id").value(40))
                .andExpect(jsonPath("$.questaoId").value(20))
                .andExpect(jsonPath("$.alternativaSelecionadaId")
                        .value(30));
    }

    @Test
    void deveFinalizarTentativa() throws Exception {
        TentativaAvaliacaoResponse response =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.FINALIZADA
                );
        response.setNota(new BigDecimal("8.00"));
        response.setAprovado(true);

        when(tentativaService.finalizar(EMAIL, 100L))
                .thenReturn(response);

        mockMvc.perform(
                        post(
                                "/api/v1/tentativas-avaliacao/100/finalizar"
                        )
                                .principal(principal())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("FINALIZADA"))
                .andExpect(jsonPath("$.nota").value(8.0))
                .andExpect(jsonPath("$.aprovado").value(true));
    }

    @Test
    void deveCorrigirRespostaDissertativa() throws Exception {
        CorrecaoRespostaRequest request =
                new CorrecaoRespostaRequest();
        request.setPontuacaoObtida(
                new BigDecimal("4.50")
        );
        request.setFeedback("Boa resposta.");

        TentativaAvaliacaoResponse response =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.FINALIZADA
                );

        when(tentativaService.corrigirResposta(
                eq(100L),
                eq(20L),
                any(CorrecaoRespostaRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/tentativas-avaliacao/100/respostas/20/correcao"
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
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status")
                        .value("FINALIZADA"));
    }

    @Test
    void deveListarMinhasTentativas() throws Exception {
        TentativaAvaliacaoResponse response =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.FINALIZADA
                );
        Page<TentativaAvaliacaoResponse> pagina =
                new PageImpl<>(
                        List.of(response),
                        PageRequest.of(0, 10),
                        1
                );

        when(tentativaService.listarMinhas(
                eq(EMAIL),
                any()
        )).thenReturn(pagina);

        mockMvc.perform(
                        get(
                                "/api/v1/tentativas-avaliacao/minhas"
                        )
                                .principal(principal())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value(100))
                .andExpect(jsonPath("$.totalElements")
                        .value(1));
    }

    @Test
    void deveListarTodasAsTentativasComFiltro() throws Exception {
        TentativaAvaliacaoResponse response =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.FINALIZADA
                );
        Page<TentativaAvaliacaoResponse> pagina =
                new PageImpl<>(List.of(response));

        when(tentativaService.listarTodos(
                eq(10L),
                any()
        )).thenReturn(pagina);

        mockMvc.perform(
                        get("/api/v1/tentativas-avaliacao")
                                .param("avaliacaoId", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value(100));
    }

    @Test
    void deveBuscarTentativaPorId() throws Exception {
        TentativaAvaliacaoResponse response =
                criarTentativaResponse(
                        100L,
                        StatusTentativa.EM_ANDAMENTO
                );

        when(tentativaService.buscarPorId(EMAIL, 100L))
                .thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/tentativas-avaliacao/100"
                        )
                                .principal(principal())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status")
                        .value("EM_ANDAMENTO"));
    }

    @Test
    void deveValidarCorrecaoSemPontuacao() throws Exception {
        CorrecaoRespostaRequest request =
                new CorrecaoRespostaRequest();
        request.setFeedback("Sem pontuação.");

        mockMvc.perform(
                        patch(
                                "/api/v1/tentativas-avaliacao/100/respostas/20/correcao"
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
                .andExpect(jsonPath("$.errors.pontuacaoObtida")
                        .value(
                                "A pontuação obtida é obrigatória."
                        ));
    }

    private Principal principal() {
        return () -> EMAIL;
    }

    private TentativaAvaliacaoResponse criarTentativaResponse(
            Long id,
            StatusTentativa status) {

        TentativaAvaliacaoResponse response =
                new TentativaAvaliacaoResponse();
        response.setId(id);
        response.setAvaliacaoId(10L);
        response.setAvaliacaoTitulo("Avaliação final");
        response.setCursoId(2L);
        response.setCursoTitulo("Informática Básica");
        response.setMatriculaId(3L);
        response.setAlunoId(1L);
        response.setAlunoNome("Aluno Teste");
        response.setNumeroTentativa(1);
        response.setStatus(status);
        return response;
    }
}
