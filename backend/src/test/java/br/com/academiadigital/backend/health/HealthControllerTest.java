package br.com.academiadigital.backend.health;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HealthControllerTest {

    private HealthController healthController;
    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        healthController = new HealthController();

        mockMvc = MockMvcBuilders
                .standaloneSetup(healthController)
                .build();
    }

    @Test
    void deveRetornarStatusDaAplicacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/health")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service")
                        .value("academia-digital-backend"))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void deveGerarTimestampNoFormatoInstant() {
        ResponseEntity<Map<String, Object>> resposta =
                healthController.health();

        assertEquals(200, resposta.getStatusCode().value());
        assertNotNull(resposta.getBody());

        Object timestamp =
                resposta.getBody().get("timestamp");

        assertNotNull(timestamp);

        assertDoesNotThrow(
                () -> Instant.parse(timestamp.toString())
        );
    }
}