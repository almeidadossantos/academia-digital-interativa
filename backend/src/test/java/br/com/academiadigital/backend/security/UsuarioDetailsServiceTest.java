package br.com.academiadigital.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.academiadigital.backend.usuario.Perfil;
import br.com.academiadigital.backend.usuario.Usuario;
import br.com.academiadigital.backend.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioDetailsService usuarioDetailsService;

    @BeforeEach
    void configurar() {
        usuarioDetailsService =
                new UsuarioDetailsService(usuarioRepository);
    }

    @Test
    void deveCarregarUsuarioExistenteComSucesso() {
        Usuario usuario = criarUsuario(
                "aluno@email.com",
                Perfil.ALUNO,
                true
        );

        when(usuarioRepository.findByEmailIgnoreCase(
                "aluno@email.com"
        )).thenReturn(Optional.of(usuario));

        UserDetails resultado =
                usuarioDetailsService.loadUserByUsername(
                        "aluno@email.com"
                );

        assertEquals(
                "aluno@email.com",
                resultado.getUsername()
        );

        assertEquals(
                "senhaCriptografada",
                resultado.getPassword()
        );

        assertTrue(resultado.isEnabled());

        verify(usuarioRepository)
                .findByEmailIgnoreCase("aluno@email.com");
    }

    @Test
    void deveNormalizarEmailAntesDeConsultarRepositorio() {
        Usuario usuario = criarUsuario(
                "aluno@email.com",
                Perfil.ALUNO,
                true
        );

        when(usuarioRepository.findByEmailIgnoreCase(
                "aluno@email.com"
        )).thenReturn(Optional.of(usuario));

        UserDetails resultado =
                usuarioDetailsService.loadUserByUsername(
                        "  ALUNO@EMAIL.COM  "
                );

        assertEquals(
                "aluno@email.com",
                resultado.getUsername()
        );

        verify(usuarioRepository)
                .findByEmailIgnoreCase("aluno@email.com");
    }

    @Test
    void deveConverterPerfilParaAutoridadeRole() {
        Usuario usuario = criarUsuario(
                "admin@academia.com",
                Perfil.ADMIN,
                true
        );

        when(usuarioRepository.findByEmailIgnoreCase(
                "admin@academia.com"
        )).thenReturn(Optional.of(usuario));

        UserDetails resultado =
                usuarioDetailsService.loadUserByUsername(
                        "admin@academia.com"
                );

        assertTrue(
                resultado.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_ADMIN")
                        )
        );
    }

    @Test
    void deveRetornarUsuarioDesabilitadoQuandoEstiverInativo() {
        Usuario usuario = criarUsuario(
                "inativo@email.com",
                Perfil.ALUNO,
                false
        );

        when(usuarioRepository.findByEmailIgnoreCase(
                "inativo@email.com"
        )).thenReturn(Optional.of(usuario));

        UserDetails resultado =
                usuarioDetailsService.loadUserByUsername(
                        "inativo@email.com"
                );

        assertFalse(resultado.isEnabled());
    }

    @Test
    void deveRetornarUsuarioDesabilitadoQuandoAtivoForNulo() {
        Usuario usuario = criarUsuario(
                "usuario@email.com",
                Perfil.ALUNO,
                null
        );

        when(usuarioRepository.findByEmailIgnoreCase(
                "usuario@email.com"
        )).thenReturn(Optional.of(usuario));

        UserDetails resultado =
                usuarioDetailsService.loadUserByUsername(
                        "usuario@email.com"
                );

        assertFalse(resultado.isEnabled());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForEncontrado() {
        when(usuarioRepository.findByEmailIgnoreCase(
                "inexistente@email.com"
        )).thenReturn(Optional.empty());

        UsernameNotFoundException excecao = assertThrows(
                UsernameNotFoundException.class,
                () -> usuarioDetailsService.loadUserByUsername(
                        "inexistente@email.com"
                )
        );

        assertEquals(
                "Usuário não encontrado.",
                excecao.getMessage()
        );

        verify(usuarioRepository)
                .findByEmailIgnoreCase(
                        "inexistente@email.com"
                );
    }

    private Usuario criarUsuario(
            String email,
            Perfil perfil,
            Boolean ativo) {

        Usuario usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("Usuário Teste");
        usuario.setEmail(email);
        usuario.setSenha("senhaCriptografada");
        usuario.setPerfil(perfil);
        usuario.setAtivo(ativo);

        return usuario;
    }
}