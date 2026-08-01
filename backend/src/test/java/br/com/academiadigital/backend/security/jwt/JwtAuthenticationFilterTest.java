package br.com.academiadigital.backend.security.jwt;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.academiadigital.backend.security.UsuarioDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioDetailsService usuarioDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void configurar() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(
                jwtService,
                usuarioDetailsService
        );

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveContinuarFiltroQuandoAuthorizationNaoForInformado()
            throws ServletException, IOException {

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, usuarioDetailsService);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );
    }

    @Test
    void deveContinuarFiltroQuandoAuthorizationNaoUsarBearer()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Basic credenciais"
        );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, usuarioDetailsService);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );
    }

    @Test
    void deveContinuarFiltroQuandoBearerNaoContiverToken()
            throws ServletException, IOException {

        request.addHeader(
                "Authorization",
                "Bearer    "
        );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, usuarioDetailsService);

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );
    }

    @Test
    void deveAutenticarUsuarioQuandoAccessTokenForValido()
            throws ServletException, IOException {

        String token = "access-token-valido";
        UserDetails userDetails = criarUsuario();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token))
                .thenReturn("aluno@email.com");

        when(usuarioDetailsService.loadUserByUsername(
                "aluno@email.com"
        )).thenReturn(userDetails);

        when(jwtService.accessTokenValido(
                token,
                userDetails
        )).thenReturn(true);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        assertTrue(authentication.isAuthenticated());
        assertSame(userDetails, authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ALUNO")
                        )
        );

        verify(jwtService).extrairEmail(token);
        verify(usuarioDetailsService)
                .loadUserByUsername("aluno@email.com");
        verify(jwtService)
                .accessTokenValido(token, userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoDeveAutenticarUsuarioQuandoAccessTokenForInvalido()
            throws ServletException, IOException {

        String token = "access-token-invalido";
        UserDetails userDetails = criarUsuario();

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token))
                .thenReturn("aluno@email.com");

        when(usuarioDetailsService.loadUserByUsername(
                "aluno@email.com"
        )).thenReturn(userDetails);

        when(jwtService.accessTokenValido(
                token,
                userDetails
        )).thenReturn(false);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoDeveCarregarUsuarioQuandoEmailExtraidoForNulo()
            throws ServletException, IOException {

        String token = "token-sem-email";

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token))
                .thenReturn(null);

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        verify(usuarioDetailsService, never())
                .loadUserByUsername(
                        org.mockito.ArgumentMatchers.anyString()
                );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoDeveSubstituirUsuarioJaAutenticado()
            throws ServletException, IOException {

        String token = "outro-access-token";

        Authentication autenticacaoExistente =
                new UsernamePasswordAuthenticationToken(
                        "usuario-ja-autenticado",
                        null,
                        java.util.List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacaoExistente);

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token))
                .thenReturn("outro@email.com");

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertSame(
                autenticacaoExistente,
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(usuarioDetailsService, never())
                .loadUserByUsername(
                        org.mockito.ArgumentMatchers.anyString()
                );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveLimparContextoQuandoTokenGerarExcecao()
            throws ServletException, IOException {

        String token = "token-malformado";

        Authentication autenticacaoAnterior =
                new UsernamePasswordAuthenticationToken(
                        "usuario-anterior",
                        null,
                        java.util.List.of()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(autenticacaoAnterior);

        request.addHeader(
                "Authorization",
                "Bearer " + token
        );

        when(jwtService.extrairEmail(token))
                .thenThrow(
                        new IllegalArgumentException(
                                "Token inválido."
                        )
                );

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                filterChain
        );

        assertNull(
                SecurityContextHolder.getContext()
                        .getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    private UserDetails criarUsuario() {
        return User.builder()
                .username("aluno@email.com")
                .password("senhaCriptografada")
                .roles("ALUNO")
                .build();
    }
}