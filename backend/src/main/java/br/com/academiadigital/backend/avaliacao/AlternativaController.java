package br.com.academiadigital.backend.avaliacao;

import java.net.URI;
import java.util.List;

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

import br.com.academiadigital.backend.avaliacao.dto.AlternativaRequest;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaResponse;
import br.com.academiadigital.backend.avaliacao.dto.AlternativaUpdateRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/alternativas")
public class AlternativaController {

    private final AlternativaService alternativaService;

    public AlternativaController(
            AlternativaService alternativaService) {

        this.alternativaService = alternativaService;
    }

    @PostMapping
    public ResponseEntity<AlternativaResponse> criar(
            @Valid @RequestBody
            AlternativaRequest request) {

        AlternativaResponse response =
                alternativaService.criar(request);

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
    public ResponseEntity<List<AlternativaResponse>>
            listarPorQuestao(
                    @RequestParam Long questaoId) {

        return ResponseEntity.ok(
                alternativaService
                        .listarPorQuestao(questaoId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlternativaResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                alternativaService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlternativaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody
            AlternativaUpdateRequest request) {

        return ResponseEntity.ok(
                alternativaService.atualizar(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        alternativaService.excluir(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
