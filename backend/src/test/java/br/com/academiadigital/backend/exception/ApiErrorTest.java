package br.com.academiadigital.backend.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class ApiErrorTest {

    @Test
    void deveCriarApiErrorPeloConstrutorComParametros() {
        LocalDateTime timestamp =
                LocalDateTime.of(2026, 7, 31, 20, 50);

        ApiError apiError = new ApiError(
                timestamp,
                404,
                "Not Found",
                "Recurso não encontrado.",
                "/api/v1/usuarios/99"
        );

        assertEquals(timestamp, apiError.getTimestamp());
        assertEquals(404, apiError.getStatus());
        assertEquals("Not Found", apiError.getError());

        assertEquals(
                "Recurso não encontrado.",
                apiError.getMessage()
        );

        assertEquals(
                "/api/v1/usuarios/99",
                apiError.getPath()
        );

        assertNull(apiError.getErrors());
    }

    @Test
    void devePermitirPreencherCamposPelosSetters() {
        LocalDateTime timestamp =
                LocalDateTime.of(2026, 7, 31, 20, 51);

        Map<String, String> errors =
                new LinkedHashMap<>();

        errors.put(
                "email",
                "O e-mail deve ser válido."
        );

        errors.put(
                "nome",
                "O nome é obrigatório."
        );

        ApiError apiError = new ApiError();

        apiError.setTimestamp(timestamp);
        apiError.setStatus(400);
        apiError.setError("Validation Error");

        apiError.setMessage(
                "Existem campos inválidos."
        );

        apiError.setPath("/api/v1/usuarios");
        apiError.setErrors(errors);

        assertEquals(timestamp, apiError.getTimestamp());
        assertEquals(400, apiError.getStatus());

        assertEquals(
                "Validation Error",
                apiError.getError()
        );

        assertEquals(
                "Existem campos inválidos.",
                apiError.getMessage()
        );

        assertEquals(
                "/api/v1/usuarios",
                apiError.getPath()
        );

        assertEquals(errors, apiError.getErrors());

        assertEquals(
                "O e-mail deve ser válido.",
                apiError.getErrors().get("email")
        );
    }

    @Test
    void deveOmitirErrorsDoJsonQuandoForNulo()
            throws Exception {

        ApiError apiError = new ApiError(
                LocalDateTime.of(
                        2026,
                        7,
                        31,
                        20,
                        52
                ),
                400,
                "Bad Request",
                "Argumento inválido.",
                "/api/v1/teste"
        );

        ObjectMapper objectMapper =
                new ObjectMapper();

        String json =
                objectMapper.writeValueAsString(apiError);

        assertTrue(json.contains("\"status\":400"));

        assertTrue(
                json.contains(
                        "\"error\":\"Bad Request\""
                )
        );

        assertTrue(
                json.contains(
                        "\"message\":\"Argumento inválido.\""
                )
        );

        assertTrue(
                json.contains(
                        "\"path\":\"/api/v1/teste\""
                )
        );

        assertFalse(json.contains("\"errors\""));
    }
}
