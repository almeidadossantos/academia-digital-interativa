package br.com.academiadigital.backend.tentativa;

import java.net.URI;
import java.security.Principal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
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

import br.com.academiadigital.backend.tentativa.dto.CorrecaoRespostaRequest;
import br.com.academiadigital.backend.tentativa.dto.RespostaQuestaoRequest;
import br.com.academiadigital.backend.tentativa.dto.RespostaQuestaoResponse;
import br.com.academiadigital.backend.tentativa.dto.TentativaAvaliacaoResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/tentativas-avaliacao")
public class TentativaAvaliacaoController {

    private final TentativaAvaliacaoService tentativaService;

    public TentativaAvaliacaoController(
            TentativaAvaliacaoService tentativaService) {

        this.tentativaService = tentativaService;
    }

    @PostMapping("/avaliacoes/{avaliacaoId}/iniciar")
    public ResponseEntity<TentativaAvaliacaoResponse> iniciar(
            @PathVariable Long avaliacaoId,
            Principal principal) {

        TentativaAvaliacaoResponse response =
                tentativaService.iniciar(
                        principal.getName(),
                        avaliacaoId
                );

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/tentativas-avaliacao/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PutMapping("/{tentativaId}/respostas/{questaoId}")
    public ResponseEntity<RespostaQuestaoResponse> salvarResposta(
            @PathVariable Long tentativaId,
            @PathVariable Long questaoId,
            @Valid @RequestBody RespostaQuestaoRequest request,
            Principal principal) {

        return ResponseEntity.ok(
                tentativaService.salvarResposta(
                        principal.getName(),
                        tentativaId,
                        questaoId,
                        request
                )
        );
    }

    @PostMapping("/{tentativaId}/finalizar")
    public ResponseEntity<TentativaAvaliacaoResponse> finalizar(
            @PathVariable Long tentativaId,
            Principal principal) {

        return ResponseEntity.ok(
                tentativaService.finalizar(
                        principal.getName(),
                        tentativaId
                )
        );
    }

    @PatchMapping(
            "/{tentativaId}/respostas/{questaoId}/correcao"
    )
    public ResponseEntity<TentativaAvaliacaoResponse>
            corrigirResposta(
                    @PathVariable Long tentativaId,
                    @PathVariable Long questaoId,
                    @Valid @RequestBody
                    CorrecaoRespostaRequest request) {

        return ResponseEntity.ok(
                tentativaService.corrigirResposta(
                        tentativaId,
                        questaoId,
                        request
                )
        );
    }

    @GetMapping("/minhas")
    public ResponseEntity<Page<TentativaAvaliacaoResponse>>
            listarMinhas(
                    Principal principal,
                    @PageableDefault(
                            size = 10,
                            sort = "dataInicio",
                            direction = Sort.Direction.DESC
                    )
                    Pageable pageable) {

        return ResponseEntity.ok(
                tentativaService.listarMinhas(
                        principal.getName(),
                        pageable
                )
        );
    }

    @GetMapping
    public ResponseEntity<Page<TentativaAvaliacaoResponse>>
            listarTodos(
                    @RequestParam(required = false)
                    Long avaliacaoId,
                    @PageableDefault(
                            size = 10,
                            sort = "dataInicio",
                            direction = Sort.Direction.DESC
                    )
                    Pageable pageable) {

        return ResponseEntity.ok(
                tentativaService.listarTodos(
                        avaliacaoId,
                        pageable
                )
        );
    }

    @GetMapping("/{tentativaId}")
    public ResponseEntity<TentativaAvaliacaoResponse> buscarPorId(
            @PathVariable Long tentativaId,
            Principal principal) {

        return ResponseEntity.ok(
                tentativaService.buscarPorId(
                        principal.getName(),
                        tentativaId
                )
        );
    }
}
