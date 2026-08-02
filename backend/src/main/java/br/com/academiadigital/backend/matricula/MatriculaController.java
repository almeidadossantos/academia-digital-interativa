package br.com.academiadigital.backend.matricula;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.academiadigital.backend.matricula.dto.MatriculaRequest;
import br.com.academiadigital.backend.matricula.dto.MatriculaResponse;
import br.com.academiadigital.backend.matricula.dto.MatriculaStatusUpdateRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(
            MatriculaService matriculaService) {

        this.matriculaService = matriculaService;
    }

    @PostMapping
    public ResponseEntity<MatriculaResponse> criar(
            @Valid
            @RequestBody
            MatriculaRequest request) {

        MatriculaResponse response =
                matriculaService.criar(request);

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
    public ResponseEntity<Page<MatriculaResponse>> listarTodos(
            @RequestParam(required = false)
            Long alunoId,

            @RequestParam(required = false)
            Long cursoId,

            @RequestParam(required = false)
            StatusMatricula status,

            @PageableDefault(
                    size = 10,
                    sort = "dataMatricula",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        Page<MatriculaResponse> pagina =
                matriculaService.listarTodos(
                        alunoId,
                        cursoId,
                        status,
                        pageable
                );

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatriculaResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                matriculaService.buscarPorId(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<MatriculaResponse> atualizarStatus(
            @PathVariable Long id,

            @Valid
            @RequestBody
            MatriculaStatusUpdateRequest request) {

        return ResponseEntity.ok(
                matriculaService.atualizarStatus(
                        id,
                        request
                )
        );
    }
}