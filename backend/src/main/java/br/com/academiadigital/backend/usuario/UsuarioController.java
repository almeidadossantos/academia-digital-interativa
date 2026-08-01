package br.com.academiadigital.backend.usuario;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.academiadigital.backend.usuario.dto.UsuarioRequest;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;
import br.com.academiadigital.backend.usuario.dto.UsuarioUpdateRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(
        @Valid @RequestBody UsuarioRequest request) {

        UsuarioResponse response = usuarioService.criar(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

@GetMapping
public ResponseEntity<Page<UsuarioResponse>> listarTodos(
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String email,
        @RequestParam(required = false) String perfil,
        @RequestParam(required = false) Boolean ativo,
        @PageableDefault(
                page = 0,
                size = 10,
                sort = "id",
                direction = Sort.Direction.ASC
        )
        Pageable pageable) {

    return ResponseEntity.ok(
            usuarioService.listarTodos(
                    nome,
                    email,
                    perfil,
                    ativo,
                    pageable
            )
    );
}
    @GetMapping("/{id}")
public ResponseEntity<UsuarioResponse> buscarPorId(
        @PathVariable Long id) {

    return ResponseEntity.ok(
            usuarioService.buscarPorId(id)
    );
}

@PutMapping("/{id}")
public ResponseEntity<UsuarioResponse> atualizar(
        @PathVariable Long id,
        @Valid @RequestBody UsuarioUpdateRequest request) {

    return ResponseEntity.ok(
            usuarioService.atualizar(id, request)
    );
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> excluir(@PathVariable Long id) {
    usuarioService.excluir(id);

    return ResponseEntity.noContent().build();
}
}