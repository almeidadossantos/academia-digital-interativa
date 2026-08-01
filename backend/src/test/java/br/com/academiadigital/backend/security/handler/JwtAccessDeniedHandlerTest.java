package br.com.academiadigital.backend.security.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.ServletException;

class JwtAccessDeniedHandlerTest {

    private JwtAccessDeniedHandler accessDeniedHandler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void configurar() {
        accessDeniedHandler = new JwtAccessDeniedHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void deveRetornarStatusAcessoNegado()
            throws IOException, ServletException {

        request.setRequestURI("/api/v1/usuarios");

        accessDeniedHandler.handle(
                request,
                response,
                new AccessDeniedException(
                        "Usuário sem permissão."
                )
        );

        assertEquals(403, response.getStatus());
    }

    @Test
    void deveRetornarRespostaJsonComCodificacaoUtf8()
            throws IOException, ServletException {

        request.setRequestURI("/api/v1/usuarios");

        accessDeniedHandler.handle(
                request,
                response,
                new AccessDeniedException(
                        "Usuário sem permissão."
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

        accessDeniedHandler.handle(
                request,
                response,
                new AccessDeniedException(
                        "Perfil sem permissão."
                )
        );

        String corpo = response.getContentAsString();

        assertTrue(corpo.contains("\"status\": 403"));

        assertTrue(
                corpo.contains(
                        "\"erro\": \"Acesso negado\""
                )
        );

        assertTrue(
                corpo.contains(
                        "\"mensagem\": " +
                        "\"Você não possui permissão para acessar este recurso.\""
                )
        );

        assertTrue(
                corpo.contains(
                        "\"caminho\": \"/api/v1/usuarios/10\""
                )
        );
    }
}