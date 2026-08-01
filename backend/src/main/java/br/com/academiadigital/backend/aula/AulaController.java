package br.com.academiadigital.backend.aula;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.academiadigital.backend.aula.dto.AulaRequest;
import br.com.academiadigital.backend.aula.dto.AulaResponse;
import br.com.academiadigital.backend.aula.dto.AulaUpdateRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/aulas")
public class AulaController {

    private final AulaService aulaService;

    public AulaController(AulaService aulaService) {
        this.aulaService = aulaService;
    }

    @PostMapping
    public ResponseEntity<AulaResponse> criar(
            @Valid @RequestBody AulaRequest request) {

        AulaResponse response = aulaService.criar(request);

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
    public ResponseEntity<Page<AulaResponse>> listarTodos(
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) StatusAula status,
            @RequestParam(required = false) String titulo,
            @PageableDefault(
                    size = 10,
                    sort = "ordem",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable) {

        Page<AulaResponse> pagina = aulaService.listarTodos(
                cursoId,
                status,
                titulo,
                pageable
        );

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AulaResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                aulaService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AulaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AulaUpdateRequest request) {

        return ResponseEntity.ok(
                aulaService.atualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        aulaService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}