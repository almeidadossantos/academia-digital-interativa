package br.com.academiadigital.backend.security.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.auth.dto.LoginRequest;
import br.com.academiadigital.backend.security.auth.dto.RefreshTokenRequest;
import br.com.academiadigital.backend.security.auth.dto.RegisterRequest;
import br.com.academiadigital.backend.security.auth.dto.TokenResponse;
import br.com.academiadigital.backend.security.jwt.JwtService;
import br.com.academiadigital.backend.usuario.UsuarioService;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioDetailsService usuarioDetailsService;

    @Mock
    private UsuarioService usuarioService;

    private AuthService authService;

    @BeforeEach
    void configurar() {
        authService = new AuthService(
                authenticationManager,
                jwtService,
                usuarioDetailsService,
                usuarioService
        );
    }

    @Test
    void deveRegistrarAlunoComSucesso() {
        RegisterRequest request = criarRegisterRequest();
        UsuarioResponse respostaEsperada = mock(UsuarioResponse.class);

        when(usuarioService.registrarAluno(
                "Aluno Teste",
                "aluno.teste@email.com",
                "Senha123!"
        )).thenReturn(respostaEsperada);

        UsuarioResponse resposta = authService.registrar(request);

        assertSame(respostaEsperada, resposta);

        verify(usuarioService).registrarAluno(
                "Aluno Teste",
                "aluno.teste@email.com",
                "Senha123!"
        );
    }

    @Test
    void deveNormalizarEmailAoFazerLogin() {
        LoginRequest request = criarLoginRequest();
        request.setEmail("  ALUNO.TESTE@EMAIL.COM  ");

        UserDetails userDetails = criarUserDetails();
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        configurarGeracaoDeTokens(userDetails);

        authService.login(request);

        ArgumentCaptor<Authentication> captor =
                ArgumentCaptor.forClass(Authentication.class);

        verify(authenticationManager).authenticate(captor.capture());

        Authentication autenticacaoEnviada = captor.getValue();

        assertEquals(
                "aluno.teste@email.com",
                autenticacaoEnviada.getPrincipal()
        );

        assertEquals(
                "Senha123!",
                autenticacaoEnviada.getCredentials()
        );
    }

    @Test
    void deveGerarTokensAoFazerLogin() {
        LoginRequest request = criarLoginRequest();
        UserDetails userDetails = criarUserDetails();
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
        )).thenReturn(authentication);

        when(authentication.getPrincipal())
                .thenReturn(userDetails);

        configurarGeracaoDeTokens(userDetails);

        TokenResponse resposta = authService.login(request);

        assertEquals("access-token", resposta.getAccessToken());
        assertEquals("refresh-token", resposta.getRefreshToken());
        assertEquals("Bearer", resposta.getTipo());
        assertEquals(900L, resposta.getExpiresIn());

        verify(jwtService).gerarAccessToken(userDetails);
        verify(jwtService).gerarRefreshToken(userDetails);
        verify(jwtService).getAccessTokenExpiration();
    }

    @Test
    void devePropagarErroDeCredenciaisInvalidasNoLogin() {
        LoginRequest request = criarLoginRequest();

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException(
                        "Credenciais inválidas."
                ));

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(request)
        );
    }

    @Test
    void deveRenovarTokensComSucesso() {
        RefreshTokenRequest request =
                criarRefreshTokenRequest("refresh-token-antigo");

        UserDetails userDetails = criarUserDetails();

        when(jwtService.extrairEmail("refresh-token-antigo"))
                .thenReturn("aluno.teste@email.com");

        when(usuarioDetailsService.loadUserByUsername(
                "aluno.teste@email.com"
        )).thenReturn(userDetails);

        when(jwtService.refreshTokenValido(
                "refresh-token-antigo",
                userDetails
        )).thenReturn(true);

        configurarGeracaoDeTokens(userDetails);

        TokenResponse resposta = authService.renovarToken(request);

        assertEquals("access-token", resposta.getAccessToken());
        assertEquals("refresh-token", resposta.getRefreshToken());
        assertEquals("Bearer", resposta.getTipo());
        assertEquals(900L, resposta.getExpiresIn());

        verify(jwtService).extrairEmail("refresh-token-antigo");
        verify(usuarioDetailsService).loadUserByUsername(
                "aluno.teste@email.com"
        );
        verify(jwtService).refreshTokenValido(
                "refresh-token-antigo",
                userDetails
        );
    }

    @Test
    void deveLancarExcecaoQuandoNaoConseguirExtrairEmailDoRefreshToken() {
        RefreshTokenRequest request =
                criarRefreshTokenRequest("token-malformado");

        when(jwtService.extrairEmail("token-malformado"))
                .thenThrow(new RuntimeException("Token inválido."));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> authService.renovarToken(request)
        );

        assertEquals(
                "Refresh token inválido.",
                excecao.getMessage()
        );
    }

    @Test
    void deveLancarExcecaoQuandoValidacaoDoRefreshTokenFalhar() {
        RefreshTokenRequest request =
                criarRefreshTokenRequest("refresh-token");

        UserDetails userDetails = criarUserDetails();

        when(jwtService.extrairEmail("refresh-token"))
                .thenReturn("aluno.teste@email.com");

        when(usuarioDetailsService.loadUserByUsername(
                "aluno.teste@email.com"
        )).thenReturn(userDetails);

        when(jwtService.refreshTokenValido(
                "refresh-token",
                userDetails
        )).thenThrow(new RuntimeException("Falha ao validar token."));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> authService.renovarToken(request)
        );

        assertEquals(
                "Refresh token inválido.",
                excecao.getMessage()
        );
    }

    @Test
    void deveLancarExcecaoQuandoRefreshTokenForInvalido() {
        RefreshTokenRequest request =
                criarRefreshTokenRequest("refresh-token-invalido");

        UserDetails userDetails = criarUserDetails();

        when(jwtService.extrairEmail("refresh-token-invalido"))
                .thenReturn("aluno.teste@email.com");

        when(usuarioDetailsService.loadUserByUsername(
                "aluno.teste@email.com"
        )).thenReturn(userDetails);

        when(jwtService.refreshTokenValido(
                "refresh-token-invalido",
                userDetails
        )).thenReturn(false);

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> authService.renovarToken(request)
        );

        assertEquals(
                "Refresh token inválido.",
                excecao.getMessage()
        );
    }

    private void configurarGeracaoDeTokens(
            UserDetails userDetails) {

        when(jwtService.gerarAccessToken(userDetails))
                .thenReturn("access-token");

        when(jwtService.gerarRefreshToken(userDetails))
                .thenReturn("refresh-token");

        when(jwtService.getAccessTokenExpiration())
                .thenReturn(900_000L);
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

    private RefreshTokenRequest criarRefreshTokenRequest(
            String refreshToken) {

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);

        return request;
    }

    private UserDetails criarUserDetails() {
        return User.builder()
                .username("aluno.teste@email.com")
                .password("senhaCriptografada")
                .roles("ALUNO")
                .build();
    }
}