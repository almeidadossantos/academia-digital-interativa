package br.com.academiadigital.backend.usuario.mapper;

import br.com.academiadigital.backend.usuario.Usuario;
import br.com.academiadigital.backend.usuario.dto.UsuarioRequest;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;

public class UsuarioMapper {

    private UsuarioMapper() {
    }

    public static Usuario toEntity(UsuarioRequest request) {

        Usuario usuario = new Usuario();

        usuario.setNome(request.getNome());
        usuario.setEmail(request.getEmail());
        usuario.setSenha(request.getSenha());
        usuario.setPerfil(request.getPerfil());
        usuario.setAtivo(request.getAtivo());

        return usuario;
    }

    public static UsuarioResponse toResponse(Usuario usuario) {

        UsuarioResponse response = new UsuarioResponse();

        response.setId(usuario.getId());
        response.setNome(usuario.getNome());
        response.setEmail(usuario.getEmail());
        response.setPerfil(usuario.getPerfil());
        response.setAtivo(usuario.getAtivo());
        response.setDataCriacao(usuario.getDataCriacao());

        return response;
    }
}