package br.com.academiadigital.backend.security.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.academiadigital.backend.security.auth.dto.LoginRequest;
import br.com.academiadigital.backend.security.auth.dto.RefreshTokenRequest;
import br.com.academiadigital.backend.security.auth.dto.RegisterRequest;
import br.com.academiadigital.backend.security.auth.dto.TokenResponse;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request) {

        TokenResponse response = authService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> renovarToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        TokenResponse response =
                authService.renovarToken(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegisterRequest request) {

        UsuarioResponse response =
                authService.registrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}