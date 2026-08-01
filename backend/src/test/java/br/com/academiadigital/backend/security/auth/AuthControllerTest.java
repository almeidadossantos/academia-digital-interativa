package br.com.academiadigital.backend.security.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.academiadigital.backend.security.auth.dto.LoginRequest;
import br.com.academiadigital.backend.security.auth.dto.RefreshTokenRequest;
import br.com.academiadigital.backend.security.auth.dto.RegisterRequest;
import br.com.academiadigital.backend.security.auth.dto.TokenResponse;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtAuthenticationFilter;
import br.com.academiadigital.backend.usuario.Perfil;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint authenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler accessDeniedHandler;

    @Test
    void deveRegistrarAlunoComSucesso() throws Exception {
        RegisterRequest request = criarRegisterRequest();

        UsuarioResponse response = new UsuarioResponse();
        response.setId(8L);
        response.setNome("Aluno Teste");
        response.setEmail("aluno.teste@email.com");
        response.setPerfil(Perfil.ALUNO);
        response.setAtivo(true);

        when(authService.registrar(any(RegisterRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.nome").value("Aluno Teste"))
                .andExpect(jsonPath("$.email")
                        .value("aluno.teste@email.com"))
                .andExpect(jsonPath("$.perfil").value("ALUNO"))
                .andExpect(jsonPath("$.ativo").value(true));

        verify(authService).registrar(any(RegisterRequest.class));
    }

    @Test
    void deveFazerLoginComSucesso() throws Exception {
        LoginRequest request = criarLoginRequest();

        TokenResponse response = new TokenResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900L
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("access-token"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("refresh-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void deveRenovarTokenComSucesso() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token-antigo");

        TokenResponse response = new TokenResponse(
                "novo-access-token",
                "novo-refresh-token",
                "Bearer",
                900L
        );

        when(authService.renovarToken(
                any(RefreshTokenRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("novo-access-token"))
                .andExpect(jsonPath("$.refreshToken")
                        .value("novo-refresh-token"))
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900));

        verify(authService).renovarToken(
                any(RefreshTokenRequest.class)
        );
    }

    @Test
    void deveRetornarBadRequestQuandoCadastroForInvalido()
            throws Exception {

        RegisterRequest request = new RegisterRequest();
        request.setNome("");
        request.setEmail("email-invalido");
        request.setSenha("123");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .registrar(any(RegisterRequest.class));
    }

    @Test
    void deveRetornarBadRequestQuandoLoginForInvalido()
            throws Exception {

        LoginRequest request = new LoginRequest();
        request.setEmail("email-invalido");
        request.setSenha("");

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .login(any(LoginRequest.class));
    }

    @Test
    void deveRetornarBadRequestQuandoRefreshTokenNaoForInformado()
            throws Exception {

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("");

        mockMvc.perform(
                        post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verify(authService, never())
                .renovarToken(any(RefreshTokenRequest.class));
    }

    private RegisterRequest criarRegisterRequest() {
        RegisterRequest request = new RegisterRequest();

        request.setNome("Aluno Teste");
        request.setEmail("aluno.teste@email.com");
        request.setSenha("Senha123!");

        return request;
    }

    private LoginRequest criarLoginRequest() {
        LoginRequest request = new LoginRequest();

        request.setEmail("aluno.teste@email.com");
        request.setSenha("Senha123!");

        return request;
    }
}