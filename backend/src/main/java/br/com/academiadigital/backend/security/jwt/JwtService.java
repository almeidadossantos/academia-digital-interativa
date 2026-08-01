package br.com.academiadigital.backend.security.jwt;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private static final String CLAIM_PERFIL = "perfil";
    private static final String CLAIM_TIPO = "tipo";

    private static final String TIPO_ACCESS = "ACCESS";
    private static final String TIPO_REFRESH = "REFRESH";

    private final SecretKey chave;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-expiration}")
            long accessTokenExpiration,
            @Value("${security.jwt.refresh-token-expiration}")
            long refreshTokenExpiration) {

        byte[] chaveDecodificada =
                Decoders.BASE64.decode(secret);

        this.chave = Keys.hmacShaKeyFor(chaveDecodificada);
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String gerarAccessToken(
            UserDetails userDetails) {

        String perfil = extrairPerfil(userDetails);

        Map<String, Object> claims = Map.of(
                CLAIM_PERFIL,
                perfil,
                CLAIM_TIPO,
                TIPO_ACCESS
        );

        return gerarToken(
                claims,
                userDetails.getUsername(),
                accessTokenExpiration
        );
    }

    public String gerarRefreshToken(
            UserDetails userDetails) {

        Map<String, Object> claims = Map.of(
                CLAIM_TIPO,
                TIPO_REFRESH
        );

        return gerarToken(
                claims,
                userDetails.getUsername(),
                refreshTokenExpiration
        );
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public String extrairPerfil(String token) {
        return extrairClaims(token)
                .get(CLAIM_PERFIL, String.class);
    }

    public String extrairTipo(String token) {
        return extrairClaims(token)
                .get(CLAIM_TIPO, String.class);
    }

    public boolean tokenValido(
            String token,
            UserDetails userDetails) {

        String email = extrairEmail(token);

        return email.equalsIgnoreCase(
                userDetails.getUsername()
        ) && !tokenExpirado(token);
    }

    public boolean accessTokenValido(
            String token,
            UserDetails userDetails) {

        return tokenValido(token, userDetails)
                && TIPO_ACCESS.equals(extrairTipo(token));
    }

    public boolean refreshTokenValido(
            String token,
            UserDetails userDetails) {

        return tokenValido(token, userDetails)
                && TIPO_REFRESH.equals(extrairTipo(token));
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    private String gerarToken(
            Map<String, Object> claims,
            String email,
            long duracao) {

        Instant agora = Instant.now();
        Instant expiracao = agora.plusMillis(duracao);

        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiracao))
                .signWith(chave)
                .compact();
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean tokenExpirado(String token) {
        Date expiracao = extrairClaims(token)
                .getExpiration();

        return expiracao.before(new Date());
    }

    private String extrairPerfil(
            UserDetails userDetails) {

        return userDetails.getAuthorities()
                .stream()
                .map(authority -> authority.getAuthority())
                .filter(authority ->
                        authority.startsWith("ROLE_"))
                .map(authority ->
                        authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "O usuário não possui um perfil válido."
                        )
                );
    }
}