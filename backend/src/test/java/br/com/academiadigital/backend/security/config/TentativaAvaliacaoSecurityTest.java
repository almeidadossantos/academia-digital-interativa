package br.com.academiadigital.backend.security.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtAuthenticationFilter;
import br.com.academiadigital.backend.security.jwt.JwtService;
import br.com.academiadigital.backend.tentativa.StatusTentativa;
import br.com.academiadigital.backend.tentativa.TentativaAvaliacaoController;
import br.com.academiadigital.backend.tentativa.TentativaAvaliacaoService;
import br.com.academiadigital.backend.tentativa.dto.CorrecaoRespostaRequest;
import br.com.academiadigital.backend.tentativa.dto.TentativaAvaliacaoResponse;

@WebMvcTest(TentativaAvaliacaoController.class)
@AutoConfigureMockMvc
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class
})
class TentativaAvaliacaoSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TentativaAvaliacaoService tentativaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @BeforeEach
    void configurar() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveRetornar401AoIniciarSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/tentativas-avaliacao/avaliacoes/10/iniciar"
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verify(tentativaService, never()).iniciar(
                any(),
                any()
        );
    }

    @Test
    void devePermitirQueAlunoInicieTentativa()
            throws Exception {

        configurarTokenValido(
                "token-aluno",
                "aluno@email.com",
                "ALUNO"
        );

        TentativaAvaliacaoResponse response =
                criarResponse(StatusTentativa.EM_ANDAMENTO);

        when(tentativaService.iniciar(
                "aluno@email.com",
                10L
        )).thenReturn(response);

        mockMvc.perform(
                        post(
                                "/api/v1/tentativas-avaliacao/avaliacoes/10/iniciar"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno"
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status")
                        .value("EM_ANDAMENTO"));
    }

    @Test
    void deveBloquearProfessorAoIniciarTentativa()
            throws Exception {

        configurarTokenValido(
                "token-professor",
                "professor@email.com",
                "PROFESSOR"
        );

        mockMvc.perform(
                        post(
                                "/api/v1/tentativas-avaliacao/avaliacoes/10/iniciar"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-professor"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(tentativaService, never()).iniciar(
                any(),
                any()
        );
    }

    @Test
    void devePermitirQueProfessorCorrijaResposta()
            throws Exception {

        configurarTokenValido(
                "token-professor-correcao",
                "professor@email.com",
                "PROFESSOR"
        );

        when(tentativaService.corrigirResposta(
                eq(100L),
                eq(20L),
                any(CorrecaoRespostaRequest.class)
        )).thenReturn(
                criarResponse(StatusTentativa.FINALIZADA)
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/tentativas-avaliacao/100/respostas/20/correcao"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-correcao"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "pontuacaoObtida": 8.00,
                                          "feedback": "Boa resposta."
                                        }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("FINALIZADA"));
    }

    @Test
    void deveBloquearAlunoAoCorrigirResposta()
            throws Exception {

        configurarTokenValido(
                "token-aluno-correcao",
                "aluno@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        patch(
                                "/api/v1/tentativas-avaliacao/100/respostas/20/correcao"
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-correcao"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "pontuacaoObtida": 8.00
                                        }
                                        """
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(tentativaService, never()).corrigirResposta(
                any(),
                any(),
                any()
        );
    }

    @Test
    void devePermitirQueAdminListeTodasAsTentativas()
            throws Exception {

        configurarTokenValido(
                "token-admin-listagem",
                "admin@email.com",
                "ADMIN"
        );

        when(tentativaService.listarTodos(
                any(),
                any()
        )).thenReturn(Page.empty());

        mockMvc.perform(
                        get("/api/v1/tentativas-avaliacao")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-listagem"
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void deveBloquearAlunoAoListarTodasAsTentativas()
            throws Exception {

        configurarTokenValido(
                "token-aluno-listagem",
                "aluno@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        get("/api/v1/tentativas-avaliacao")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-listagem"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verify(tentativaService, never()).listarTodos(
                any(),
                any()
        );
    }

    private TentativaAvaliacaoResponse criarResponse(
            StatusTentativa status) {

        TentativaAvaliacaoResponse response =
                new TentativaAvaliacaoResponse();
        response.setId(100L);
        response.setStatus(status);
        response.setPontuacaoObtida(BigDecimal.ZERO);
        return response;
    }

    private void configurarTokenValido(
            String token,
            String email,
            String perfil) {

        UserDetails userDetails = User.builder()
                .username(email)
                .password("senhaCriptografada")
                .roles(perfil)
                .build();

        when(jwtService.extrairEmail(token))
                .thenReturn(email);

        when(usuarioDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);

        when(jwtService.accessTokenValido(
                token,
                userDetails
        )).thenReturn(true);
    }
}
