package br.com.academiadigital.backend.security.auth.dto;

public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private String tipo;
    private long expiresIn;

    public TokenResponse(
            String accessToken,
            String refreshToken,
            String tipo,
            long expiresIn) {

        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tipo = tipo;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTipo() {
        return tipo;
    }

    public long getExpiresIn() {
        return expiresIn;
    }
}