package br.com.academiadigital.backend.avaliacao;

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

import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.AvaliacaoUpdateRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/avaliacoes")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(
            AvaliacaoService avaliacaoService) {

        this.avaliacaoService = avaliacaoService;
    }

    @PostMapping
    public ResponseEntity<AvaliacaoResponse> criar(
            @Valid @RequestBody AvaliacaoRequest request) {

        AvaliacaoResponse response =
                avaliacaoService.criar(request);

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
    public ResponseEntity<Page<AvaliacaoResponse>> listarTodos(
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false)
            StatusAvaliacao status,
            @RequestParam(required = false) String titulo,
            @PageableDefault(
                    size = 10,
                    sort = "ordem",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable) {

        Page<AvaliacaoResponse> pagina =
                avaliacaoService.listarTodos(
                        cursoId,
                        status,
                        titulo,
                        pageable
                );

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvaliacaoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                avaliacaoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AvaliacaoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody
            AvaliacaoUpdateRequest request) {

        return ResponseEntity.ok(
                avaliacaoService.atualizar(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        avaliacaoService.excluir(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
