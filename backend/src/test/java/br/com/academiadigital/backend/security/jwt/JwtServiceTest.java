package br.com.academiadigital.backend.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private static final String SECRET =
            "YWNhZGVtaWEtZGlnaXRhbC1pbnRlcmF0aXZhLWNoYXZlLXNlY3JldGEtMjAyNg==";

    private static final long ACCESS_EXPIRATION = 900_000L;
    private static final long REFRESH_EXPIRATION = 604_800_000L;

    private JwtService jwtService;
    private UserDetails usuarioAluno;

    @BeforeEach
    void configurar() {
        jwtService = new JwtService(
                SECRET,
                ACCESS_EXPIRATION,
                REFRESH_EXPIRATION
        );

        usuarioAluno = User.builder()
                .username("aluno.teste@email.com")
                .password("senhaCriptografada")
                .roles("ALUNO")
                .build();
    }

    @Test
    void deveGerarAccessTokenComEmailPerfilETipoCorretos() {
        String token = jwtService.gerarAccessToken(usuarioAluno);

        assertEquals(
                "aluno.teste@email.com",
                jwtService.extrairEmail(token)
        );

        assertEquals(
                "ALUNO",
                jwtService.extrairPerfil(token)
        );

        assertEquals(
                "ACCESS",
                jwtService.extrairTipo(token)
        );
    }

    @Test
    void deveGerarRefreshTokenComEmailETipoCorretos() {
        String token = jwtService.gerarRefreshToken(usuarioAluno);

        assertEquals(
                "aluno.teste@email.com",
                jwtService.extrairEmail(token)
        );

        assertEquals(
                "REFRESH",
                jwtService.extrairTipo(token)
        );

        assertNull(jwtService.extrairPerfil(token));
    }

    @Test
    void deveValidarAccessTokenDoMesmoUsuario() {
        String token = jwtService.gerarAccessToken(usuarioAluno);

        assertTrue(
                jwtService.accessTokenValido(
                        token,
                        usuarioAluno
                )
        );
    }

    @Test
    void deveValidarRefreshTokenDoMesmoUsuario() {
        String token = jwtService.gerarRefreshToken(usuarioAluno);

        assertTrue(
                jwtService.refreshTokenValido(
                        token,
                        usuarioAluno
                )
        );
    }

    @Test
    void naoDeveAceitarRefreshTokenComoAccessToken() {
        String refreshToken =
                jwtService.gerarRefreshToken(usuarioAluno);

        assertFalse(
                jwtService.accessTokenValido(
                        refreshToken,
                        usuarioAluno
                )
        );
    }

    @Test
    void naoDeveAceitarAccessTokenComoRefreshToken() {
        String accessToken =
                jwtService.gerarAccessToken(usuarioAluno);

        assertFalse(
                jwtService.refreshTokenValido(
                        accessToken,
                        usuarioAluno
                )
        );
    }

    @Test
    void naoDeveValidarTokenParaOutroUsuario() {
        String token = jwtService.gerarAccessToken(usuarioAluno);

        UserDetails outroUsuario = User.builder()
                .username("outro.usuario@email.com")
                .password("outraSenhaCriptografada")
                .roles("ALUNO")
                .build();

        assertFalse(
                jwtService.tokenValido(
                        token,
                        outroUsuario
                )
        );
    }

    @Test
    void deveCompararEmailSemDiferenciarMaiusculasEMinusculas() {
        String token = jwtService.gerarAccessToken(usuarioAluno);

        UserDetails mesmoUsuarioComEmailMaiusculo =
                User.builder()
                        .username("ALUNO.TESTE@EMAIL.COM")
                        .password("senhaCriptografada")
                        .roles("ALUNO")
                        .build();

        assertTrue(
                jwtService.tokenValido(
                        token,
                        mesmoUsuarioComEmailMaiusculo
                )
        );
    }

    @Test
    void deveRetornarTempoDeExpiracaoDoAccessToken() {
        assertEquals(
                ACCESS_EXPIRATION,
                jwtService.getAccessTokenExpiration()
        );
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoPossuirRoleValida() {
        UserDetails usuarioSemRole = User.builder()
                .username("usuario@email.com")
                .password("senhaCriptografada")
                .authorities("PERMISSAO_LEITURA")
                .build();

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> jwtService.gerarAccessToken(usuarioSemRole)
        );

        assertEquals(
                "O usuário não possui um perfil válido.",
                excecao.getMessage()
        );
    }
}