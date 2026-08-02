package br.com.academiadigital.backend.matricula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class MatriculaTest {

    @Test
    void deveDefinirStatusEDatasAoPersistir() {
        Matricula matricula = new Matricula();

        matricula.prePersist();

        assertEquals(
                StatusMatricula.ATIVA,
                matricula.getStatus()
        );

        assertNotNull(matricula.getDataMatricula());
        assertNotNull(matricula.getDataAtualizacao());
        assertNull(matricula.getDataConclusao());
        assertNull(matricula.getDataCancelamento());
    }

    @Test
    void devePreservarStatusEDataMatriculaInformados() {
        Matricula matricula = new Matricula();

        LocalDateTime dataMatricula =
                LocalDateTime.of(
                        2026,
                        8,
                        1,
                        10,
                        30
                );

        matricula.setStatus(StatusMatricula.CONCLUIDA);
        matricula.setDataMatricula(dataMatricula);

        matricula.prePersist();

        assertEquals(
                StatusMatricula.CONCLUIDA,
                matricula.getStatus()
        );

        assertEquals(
                dataMatricula,
                matricula.getDataMatricula()
        );

        assertNotNull(matricula.getDataAtualizacao());
    }

    @Test
    void deveAtualizarDataAtualizacao() {
        Matricula matricula = new Matricula();

        LocalDateTime dataAnterior =
                LocalDateTime.now().minusDays(1);

        matricula.setDataAtualizacao(dataAnterior);

        matricula.preUpdate();

        assertNotNull(matricula.getDataAtualizacao());

        assertTrue(
                matricula
                        .getDataAtualizacao()
                        .isAfter(dataAnterior)
        );
    }
}