package br.com.academiadigital.backend.security.config;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.auth.AuthService;
import br.com.academiadigital.backend.security.auth.dto.LoginRequest;
import br.com.academiadigital.backend.security.auth.dto.TokenResponse;
import br.com.academiadigital.backend.security.jwt.JwtService;
import br.com.academiadigital.backend.usuario.UsuarioService;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @BeforeEach
    void configurar() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devePermitirLoginSemTokenJwtESemTokenCsrf()
            throws Exception {

        TokenResponse resposta = new TokenResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900L
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(resposta);

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "aluno@email.com",
                                          "senha": "Senha123!"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("access-token"));
    }

    @Test
    void deveRetornar401AoAcessarUsuariosSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/usuarios")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/usuarios"));
    }

    @Test
    void deveRetornar403QuandoAlunoAcessarUsuarios()
            throws Exception {

        configurarTokenValido(
                "token-aluno",
                "aluno@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        get("/api/v1/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/usuarios"));
    }

    @Test
    void devePermitirQueAdminAcesseUsuarios()
            throws Exception {

        configurarTokenValido(
                "token-admin",
                "admin@email.com",
                "ADMIN"
        );

        when(usuarioService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<UsuarioResponse>empty());

        mockMvc.perform(
                        get("/api/v1/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void naoDeveCriarSessaoAoAutenticarComJwt()
            throws Exception {

        configurarTokenValido(
                "token-admin",
                "admin@email.com",
                "ADMIN"
        );

        when(usuarioService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<UsuarioResponse>empty());

        MvcResult resultado = mockMvc.perform(
                        get("/api/v1/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk())
                .andReturn();

        assertNull(resultado.getRequest().getSession(false));
    }

    @Test
    void deveDisponibilizarBCryptPasswordEncoder() {
        String senha = "Senha123!";
        String senhaCriptografada =
                passwordEncoder.encode(senha);

        assertTrue(
                passwordEncoder.matches(
                        senha,
                        senhaCriptografada
                )
        );
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