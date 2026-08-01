package br.com.academiadigital.backend.usuario.dto;

import java.time.LocalDateTime;

import br.com.academiadigital.backend.usuario.Perfil;

public class UsuarioResponse {

    private Long id;

    private String nome;

    private String email;

    private Perfil perfil;

    private Boolean ativo;

    private LocalDateTime dataCriacao;

    public UsuarioResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Perfil getPerfil() {
    return perfil;
    }

    public void setPerfil(Perfil perfil) {
    this.perfil = perfil;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}