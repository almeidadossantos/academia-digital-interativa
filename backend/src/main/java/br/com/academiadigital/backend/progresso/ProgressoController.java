package br.com.academiadigital.backend.progresso;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.academiadigital.backend.progresso.dto.ProgressoAulaResponse;
import br.com.academiadigital.backend.progresso.dto.ProgressoCursoResponse;

@RestController
@RequestMapping("/api/v1/progressos")
public class ProgressoController {

    private final ProgressoService progressoService;

    public ProgressoController(
            ProgressoService progressoService) {

        this.progressoService = progressoService;
    }

    @GetMapping("/cursos/{cursoId}")
    public ResponseEntity<ProgressoCursoResponse>
            buscarProgressoCurso(
                    @PathVariable Long cursoId,
                    Principal principal) {

        ProgressoCursoResponse response =
                progressoService.buscarProgressoCurso(
                        principal.getName(),
                        cursoId
                );

        return ResponseEntity.ok(response);
    }
    @PostMapping("/aulas/{aulaId}/concluir")
    public ResponseEntity<ProgressoAulaResponse>
            concluirAula(
                    @PathVariable Long aulaId,
                    Principal principal) {

        ProgressoAulaResponse response =
                progressoService.concluirAula(
                        principal.getName(),
                        aulaId
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/aulas/{aulaId}/conclusao")
    public ResponseEntity<Void>
            removerConclusaoAula(
                    @PathVariable Long aulaId,
                    Principal principal) {

        progressoService.removerConclusaoAula(
                principal.getName(),
                aulaId
        );

        return ResponseEntity.noContent().build();
    }

}
