package br.com.academiadigital.backend.trilha;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TrilhaRepositoryTest {

    @Autowired
    private TrilhaRepository trilhaRepository;

    @Test
    void deveEncontrarTituloIgnorandoMaiusculasEMinusculas() {
        String titulo =
                "Trilha Java " + UUID.randomUUID();

        Trilha trilha =
                criarTrilha(titulo);

        trilhaRepository.saveAndFlush(trilha);

        boolean resultado =
                trilhaRepository.existsByTituloIgnoreCase(
                        titulo.toUpperCase()
                );

        assertTrue(resultado);
    }

    @Test
    void deveDesconsiderarAPropriaTrilhaAoVerificarTitulo()
            throws Exception {

        String titulo =
                "Trilha Spring " + UUID.randomUUID();

        Trilha trilha =
                trilhaRepository.saveAndFlush(
                        criarTrilha(titulo)
                );

        boolean resultado =
                trilhaRepository
                        .existsByTituloIgnoreCaseAndIdNot(
                                titulo.toLowerCase(),
                                trilha.getId()
                        );

        assertFalse(resultado);
    }

    @Test
    void deveEncontrarOutraTrilhaComMesmoTitulo() {
        String tituloExistente =
                "Trilha Backend " + UUID.randomUUID();

        Trilha trilhaExistente =
                trilhaRepository.saveAndFlush(
                        criarTrilha(tituloExistente)
                );

        Trilha outraTrilha =
                trilhaRepository.saveAndFlush(
                        criarTrilha(
                                "Outra trilha "
                                        + UUID.randomUUID()
                        )
                );

        boolean resultado =
                trilhaRepository
                        .existsByTituloIgnoreCaseAndIdNot(
                                trilhaExistente
                                        .getTitulo()
                                        .toUpperCase(),
                                outraTrilha.getId()
                        );

        assertTrue(resultado);
    }

    private Trilha criarTrilha(
            String titulo) {

        Trilha trilha = new Trilha();

        trilha.setTitulo(titulo);
        trilha.setDescricao(
                "Descrição utilizada no teste "
                        + "de integração do repositório."
        );
        trilha.setStatus(
                StatusTrilha.RASCUNHO
        );

        return trilha;
    }
}