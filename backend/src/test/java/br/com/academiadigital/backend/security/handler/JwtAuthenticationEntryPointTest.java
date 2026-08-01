package br.com.academiadigital.backend.security.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import jakarta.servlet.ServletException;

class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void configurar() {
        entryPoint = new JwtAuthenticationEntryPoint();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void deveRetornarStatusNaoAutorizado()
            throws IOException, ServletException {

        request.setRequestURI("/api/v1/usuarios");

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException(
                        "Credenciais inválidas."
                )
        );

        assertEquals(401, response.getStatus());
    }

    @Test
    void deveRetornarRespostaJsonComCodificacaoUtf8()
            throws IOException, ServletException {

        request.setRequestURI("/api/v1/usuarios");

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException(
                        "Credenciais inválidas."
                )
        );

        assertTrue(
                response.getContentType()
                        .startsWith("application/json")
        );

        assertEquals(
                "UTF-8",
                response.getCharacterEncoding()
        );
    }

    @Test
    void deveRetornarCorpoComDadosDoErroECaminhoDaRequisicao()
            throws IOException, ServletException {

        request.setRequestURI("/api/v1/usuarios/10");

        entryPoint.commence(
                request,
                response,
                new BadCredentialsException(
                        "Token inválido."
                )
        );

        String corpo = response.getContentAsString();

        assertTrue(corpo.contains("\"status\": 401"));
        assertTrue(
                corpo.contains(
                        "\"erro\": \"Não autorizado\""
                )
        );
        assertTrue(
                corpo.contains(
                        "\"mensagem\": " +
                        "\"Autenticação necessária ou token inválido.\""
                )
        );
        assertTrue(
                corpo.contains(
                        "\"caminho\": \"/api/v1/usuarios/10\""
                )
        );
    }
}