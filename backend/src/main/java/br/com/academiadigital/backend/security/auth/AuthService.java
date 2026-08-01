package br.com.academiadigital.backend.security.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.auth.dto.LoginRequest;
import br.com.academiadigital.backend.security.auth.dto.RefreshTokenRequest;
import br.com.academiadigital.backend.security.auth.dto.RegisterRequest;
import br.com.academiadigital.backend.security.auth.dto.TokenResponse;
import br.com.academiadigital.backend.security.jwt.JwtService;
import br.com.academiadigital.backend.usuario.UsuarioService;
import br.com.academiadigital.backend.usuario.dto.UsuarioRequest;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioDetailsService usuarioDetailsService,
            UsuarioService usuarioService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
        this.usuarioService = usuarioService;
    }

   public UsuarioResponse registrar(RegisterRequest request) {

    return usuarioService.registrarAluno(
            request.getNome(),
            request.getEmail(),
            request.getSenha()
    );
}

    public TokenResponse login(LoginRequest request) {

        String emailNormalizado = request.getEmail()
                .trim()
                .toLowerCase();

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                emailNormalizado,
                                request.getSenha()
                        )
                );

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        return gerarRespostaComTokens(userDetails);
    }

    public TokenResponse renovarToken(
            RefreshTokenRequest request) {

        String refreshToken = request.getRefreshToken();

        String email;

        try {
            email = jwtService.extrairEmail(refreshToken);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "Refresh token inválido."
            );
        }

        UserDetails userDetails =
                usuarioDetailsService.loadUserByUsername(email);

        boolean refreshTokenValido;

        try {
            refreshTokenValido =
                    jwtService.refreshTokenValido(
                            refreshToken,
                            userDetails
                    );
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "Refresh token inválido."
            );
        }

        if (!refreshTokenValido) {
            throw new IllegalArgumentException(
                    "Refresh token inválido."
            );
        }

        return gerarRespostaComTokens(userDetails);
    }

    private TokenResponse gerarRespostaComTokens(
            UserDetails userDetails) {

        String accessToken =
                jwtService.gerarAccessToken(userDetails);

        String refreshToken =
                jwtService.gerarRefreshToken(userDetails);

        long expiresIn =
                jwtService.getAccessTokenExpiration() / 1000;

        return new TokenResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn
        );
    }
}