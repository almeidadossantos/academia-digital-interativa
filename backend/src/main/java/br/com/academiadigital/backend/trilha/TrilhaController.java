package br.com.academiadigital.backend.trilha;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.academiadigital.backend.trilha.dto.TrilhaCursoOrdemRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaCursoResponse;
import br.com.academiadigital.backend.trilha.dto.TrilhaRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/trilhas")
public class TrilhaController {

    private final TrilhaService trilhaService;

    public TrilhaController(
            TrilhaService trilhaService) {

        this.trilhaService = trilhaService;
    }

    @PostMapping
    public ResponseEntity<TrilhaResponse> criar(
            @Valid @RequestBody TrilhaRequest request) {

        TrilhaResponse response =
                trilhaService.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TrilhaResponse>> listarTodos(
            @RequestParam(required = false)
            String titulo,

            @RequestParam(required = false)
            StatusTrilha status,

            @PageableDefault(
                    size = 10,
                    sort = "titulo",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable) {

        Page<TrilhaResponse> pagina =
                trilhaService.listarTodos(
                        titulo,
                        status,
                        pageable
                );

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrilhaResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                trilhaService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrilhaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody TrilhaRequest request) {

        return ResponseEntity.ok(
                trilhaService.atualizar(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        trilhaService.excluir(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{trilhaId}/cursos")
    public ResponseEntity<TrilhaCursoResponse> adicionarCurso(
            @PathVariable Long trilhaId,

            @Valid
            @RequestBody
            TrilhaCursoRequest request) {

        TrilhaCursoResponse response =
                trilhaService.adicionarCurso(
                        trilhaId,
                        request
                );

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{cursoId}")
                .buildAndExpand(response.getCursoId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PatchMapping(
            "/{trilhaId}/cursos/{cursoId}/ordem"
    )
    public ResponseEntity<TrilhaCursoResponse> atualizarOrdem(
            @PathVariable Long trilhaId,
            @PathVariable Long cursoId,

            @Valid
            @RequestBody
            TrilhaCursoOrdemRequest request) {

        return ResponseEntity.ok(
                trilhaService.atualizarOrdem(
                        trilhaId,
                        cursoId,
                        request
                )
        );
    }

    @DeleteMapping(
            "/{trilhaId}/cursos/{cursoId}"
    )
    public ResponseEntity<Void> removerCurso(
            @PathVariable Long trilhaId,
            @PathVariable Long cursoId) {

        trilhaService.removerCurso(
                trilhaId,
                cursoId
        );

        return ResponseEntity.noContent().build();
    }
}