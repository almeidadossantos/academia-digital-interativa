package br.com.academiadigital.backend.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void deveDefinirAtivoEDataCriacaoAntesDePersistirQuandoForemNulos() {
        Usuario usuario = new Usuario();

        usuario.prePersist();

        assertTrue(usuario.getAtivo());
        assertNotNull(usuario.getDataCriacao());
    }

    @Test
    void devePreservarAtivoEDataCriacaoQuandoJaEstiveremDefinidos() {
        Usuario usuario = new Usuario();

        usuario.prePersist();

        LocalDateTime dataCriacaoOriginal =
                usuario.getDataCriacao();

        usuario.setAtivo(false);

        usuario.prePersist();

        assertFalse(usuario.getAtivo());

        assertEquals(
                dataCriacaoOriginal,
                usuario.getDataCriacao()
        );
    }
}