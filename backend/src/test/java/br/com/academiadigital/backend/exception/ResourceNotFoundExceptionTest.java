package br.com.academiadigital.backend.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ResourceNotFoundExceptionTest {

    @Test
    void deveArmazenarMensagemInformadaNoConstrutor() {
        ResourceNotFoundException excecao =
                new ResourceNotFoundException(
                        "Recurso não encontrado."
                );

        assertEquals(
                "Recurso não encontrado.",
                excecao.getMessage()
        );
    }

    @Test
    void deveSerUmaExcecaoDeTempoDeExecucao() {
        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> {
                            throw new ResourceNotFoundException(
                                    "Usuário não encontrado."
                            );
                        }
                );

        assertInstanceOf(
                RuntimeException.class,
                excecao
        );

        assertEquals(
                "Usuário não encontrado.",
                excecao.getMessage()
        );
    }
}