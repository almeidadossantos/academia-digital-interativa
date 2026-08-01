package br.com.academiadigital.backend.curso;

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

import br.com.academiadigital.backend.curso.dto.CursoRequest;
import br.com.academiadigital.backend.curso.dto.CursoResponse;
import br.com.academiadigital.backend.curso.dto.CursoUpdateRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @PostMapping
    public ResponseEntity<CursoResponse> criar(
            @Valid @RequestBody CursoRequest request) {

        CursoResponse response = cursoService.criar(request);

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
    public ResponseEntity<Page<CursoResponse>> listarTodos(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) NivelCurso nivel,
            @RequestParam(required = false) StatusCurso status,
            @PageableDefault(
                    size = 10,
                    sort = "titulo",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable) {

        Page<CursoResponse> pagina = cursoService.listarTodos(
                titulo,
                nivel,
                status,
                pageable
        );

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                cursoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody CursoUpdateRequest request) {

        return ResponseEntity.ok(
                cursoService.atualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        cursoService.excluir(id);

        return ResponseEntity.noContent().build();
    }
}