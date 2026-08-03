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

import br.com.academiadigital.backend.avaliacao.dto.QuestaoRequest;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoResponse;
import br.com.academiadigital.backend.avaliacao.dto.QuestaoUpdateRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/questoes")
public class QuestaoController {

    private final QuestaoService questaoService;

    public QuestaoController(
            QuestaoService questaoService) {

        this.questaoService = questaoService;
    }

    @PostMapping
    public ResponseEntity<QuestaoResponse> criar(
            @Valid @RequestBody QuestaoRequest request) {

        QuestaoResponse response =
                questaoService.criar(request);

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
    public ResponseEntity<Page<QuestaoResponse>> listarTodos(
            @RequestParam(required = false)
            Long avaliacaoId,
            @RequestParam(required = false)
            TipoQuestao tipo,
            @RequestParam(required = false)
            String enunciado,
            @PageableDefault(
                    size = 10,
                    sort = "ordem",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable) {

        Page<QuestaoResponse> pagina =
                questaoService.listarTodos(
                        avaliacaoId,
                        tipo,
                        enunciado,
                        pageable
                );

        return ResponseEntity.ok(pagina);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestaoResponse> buscarPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                questaoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestaoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody
            QuestaoUpdateRequest request) {

        return ResponseEntity.ok(
                questaoService.atualizar(
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id) {

        questaoService.excluir(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}
