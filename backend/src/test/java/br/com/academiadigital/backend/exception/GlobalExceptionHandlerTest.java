package br.com.academiadigital.backend.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(
                        new GlobalExceptionHandler()
                )
                .setValidator(validator)
                .build();
    }

    @Test
    void deveRetornarBadRequestParaIllegalArgumentException()
            throws Exception {

        mockMvc.perform(
                        get("/teste/argumento-invalido")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Argumento inválido."))
                .andExpect(jsonPath("$.path")
                        .value("/teste/argumento-invalido"));
    }

    @Test
    void deveRetornarNotFoundParaResourceNotFoundException()
            throws Exception {

        mockMvc.perform(
                        get("/teste/recurso-inexistente")
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Recurso não encontrado."))
                .andExpect(jsonPath("$.path")
                        .value("/teste/recurso-inexistente"));
    }

    @Test
    void deveRetornarErrosDosCamposInvalidos()
            throws Exception {

        mockMvc.perform(
                        post("/teste/validacao")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "nome": "",
                                          "email": "email-invalido"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Validation Error"))
                .andExpect(jsonPath("$.message")
                        .value("Existem campos inválidos."))
                .andExpect(jsonPath("$.path")
                        .value("/teste/validacao"))
                .andExpect(jsonPath("$.errors.nome")
                        .value("O nome é obrigatório."))
                .andExpect(jsonPath("$.errors.email")
                        .value("O e-mail deve ser válido."));
    }

    @RestController
    @RequestMapping("/teste")
    static class TestController {

        @GetMapping("/argumento-invalido")
        String gerarIllegalArgumentException() {
            throw new IllegalArgumentException(
                    "Argumento inválido."
            );
        }

        @GetMapping("/recurso-inexistente")
        String gerarResourceNotFoundException() {
            throw new ResourceNotFoundException(
                    "Recurso não encontrado."
            );
        }

        @PostMapping("/validacao")
        TestRequest validar(
                @Valid @RequestBody TestRequest request) {

            return request;
        }
    }

    static class TestRequest {

        @NotBlank(message = "O nome é obrigatório.")
        private String nome;

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail deve ser válido.")
        private String email;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
