package br.com.academiadigital.backend.usuario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.usuario.dto.UsuarioRequest;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;
import br.com.academiadigital.backend.usuario.dto.UsuarioUpdateRequest;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    @BeforeEach
    void configurar() {
        usuarioService = new UsuarioService(
                usuarioRepository,
                passwordEncoder
        );
    }

    @Test
    void deveBuscarUsuarioPorId() {
        Usuario usuario = criarUsuario();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        UsuarioResponse resposta =
                usuarioService.buscarPorId(1L);

        assertEquals(1L, resposta.getId());
        assertEquals("João Silva", resposta.getNome());
        assertEquals("joao@email.com", resposta.getEmail());
        assertEquals(Perfil.ALUNO, resposta.getPerfil());
        assertTrue(resposta.getAtivo());

        verify(usuarioRepository).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExistir() {
        when(usuarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.buscarPorId(999L)
                );

        assertEquals(
                "Usuário não encontrado com o ID: 999",
                excecao.getMessage()
        );

        verify(usuarioRepository).findById(999L);
    }

    @Test
    void deveCriarUsuarioComSucesso() {
        UsuarioRequest request = criarUsuarioRequest();

        when(usuarioRepository.existsByEmailIgnoreCase(
                "joao@email.com"
        )).thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("senhaCriptografada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> {
                    Usuario usuario =
                            invocacao.getArgument(0);

                    usuario.setId(1L);
                    return usuario;
                });

        UsuarioResponse resposta =
                usuarioService.criar(request);

        assertEquals(1L, resposta.getId());
        assertEquals("João Silva", resposta.getNome());
        assertEquals("joao@email.com", resposta.getEmail());
        assertEquals(Perfil.ALUNO, resposta.getPerfil());
        assertTrue(resposta.getAtivo());

        verify(usuarioRepository)
                .existsByEmailIgnoreCase("joao@email.com");

        verify(passwordEncoder).encode("123456");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveAplicarPerfilEAtivoPadraoAoCriarUsuario() {
        UsuarioRequest request = criarUsuarioRequest();
        request.setPerfil(null);
        request.setAtivo(null);

        when(usuarioRepository.existsByEmailIgnoreCase(
                "joao@email.com"
        )).thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("senhaCriptografada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> {
                    Usuario usuario =
                            invocacao.getArgument(0);

                    usuario.setId(1L);
                    return usuario;
                });

        UsuarioResponse resposta =
                usuarioService.criar(request);

        assertEquals(Perfil.ALUNO, resposta.getPerfil());
        assertTrue(resposta.getAtivo());
    }

    @Test
    void deveNormalizarEmailAoCriarUsuario() {
        UsuarioRequest request = criarUsuarioRequest();
        request.setEmail("  JOAO@EMAIL.COM  ");

        when(usuarioRepository.existsByEmailIgnoreCase(
                "joao@email.com"
        )).thenReturn(false);

        when(passwordEncoder.encode("123456"))
                .thenReturn("senhaCriptografada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> {
                    Usuario usuario =
                            invocacao.getArgument(0);

                    usuario.setId(1L);
                    return usuario;
                });

        UsuarioResponse resposta =
                usuarioService.criar(request);

        assertEquals(
                "joao@email.com",
                resposta.getEmail()
        );

        verify(usuarioRepository)
                .existsByEmailIgnoreCase("joao@email.com");
    }

    @Test
    void deveLancarExcecaoAoCriarUsuarioComEmailDuplicado() {
        UsuarioRequest request = criarUsuarioRequest();

        when(usuarioRepository.existsByEmailIgnoreCase(
                "joao@email.com"
        )).thenReturn(true);

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.criar(request)
                );

        assertEquals(
                "Já existe um usuário com este e-mail.",
                excecao.getMessage()
        );

        verify(usuarioRepository)
                .existsByEmailIgnoreCase("joao@email.com");

        verify(usuarioRepository, never())
                .save(any(Usuario.class));

        verify(passwordEncoder, never())
                .encode(any(String.class));
    }

    @Test
    void deveRegistrarAlunoComSucesso() {
        when(usuarioRepository.existsByEmailIgnoreCase(
                "aluno@email.com"
        )).thenReturn(false);

        when(passwordEncoder.encode("Senha123!"))
                .thenReturn("senhaCriptografada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacao -> {
                    Usuario usuario =
                            invocacao.getArgument(0);

                    usuario.setId(10L);
                    return usuario;
                });

        UsuarioResponse resposta =
                usuarioService.registrarAluno(
                        " Aluno Teste ",
                        " ALUNO@EMAIL.COM ",
                        "Senha123!"
                );

        assertEquals(10L, resposta.getId());
        assertEquals("Aluno Teste", resposta.getNome());

        assertEquals(
                "aluno@email.com",
                resposta.getEmail()
        );

        assertEquals(Perfil.ALUNO, resposta.getPerfil());
        assertTrue(resposta.getAtivo());

        verify(usuarioRepository)
                .existsByEmailIgnoreCase(
                        "aluno@email.com"
                );

        verify(passwordEncoder).encode("Senha123!");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoAoRegistrarAlunoComEmailDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase(
                "aluno@email.com"
        )).thenReturn(true);

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.registrarAluno(
                                "Aluno Teste",
                                " ALUNO@EMAIL.COM ",
                                "Senha123!"
                        )
                );

        assertEquals(
                "Já existe um usuário com este e-mail.",
                excecao.getMessage()
        );

        verify(passwordEncoder, never())
                .encode(any(String.class));

        verify(usuarioRepository, never())
                .save(any(Usuario.class));
    }

    @Test
    void deveListarUsuariosComPaginacaoEFiltros() {
        Usuario primeiroUsuario = criarUsuario();

        Usuario segundoUsuario = new Usuario();
        segundoUsuario.setId(2L);
        segundoUsuario.setNome("Maria Souza");
        segundoUsuario.setEmail("maria@email.com");
        segundoUsuario.setSenha("senhaCriptografada");
        segundoUsuario.setPerfil(Perfil.PROFESSOR);
        segundoUsuario.setAtivo(true);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Usuario> paginaUsuarios =
                new PageImpl<>(
                        List.of(
                                primeiroUsuario,
                                segundoUsuario
                        ),
                        pageable,
                        2
                );

        when(usuarioRepository.findAll(
                ArgumentMatchers
                        .<Specification<Usuario>>any(),
                eq(pageable)
        )).thenReturn(paginaUsuarios);

        Page<UsuarioResponse> resposta =
                usuarioService.listarTodos(
                        "a",
                        "@email.com",
                        "ALUNO",
                        true,
                        pageable
                );

        assertEquals(2, resposta.getTotalElements());
        assertEquals(2, resposta.getContent().size());
        assertEquals(0, resposta.getNumber());
        assertEquals(10, resposta.getSize());

        assertEquals(
                "João Silva",
                resposta.getContent().get(0).getNome()
        );

        assertEquals(
                "Maria Souza",
                resposta.getContent().get(1).getNome()
        );

        verify(usuarioRepository).findAll(
                ArgumentMatchers
                        .<Specification<Usuario>>any(),
                eq(pageable)
        );
    }

    @Test
    void deveAtualizarUsuarioComSucesso() {
        Usuario usuario = criarUsuario();

        UsuarioUpdateRequest request =
                criarUsuarioUpdateRequest();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.findByEmailIgnoreCase(
                "maria@email.com"
        )).thenReturn(Optional.empty());

        when(passwordEncoder.encode("novaSenha"))
                .thenReturn("novaSenhaCriptografada");

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        UsuarioResponse resposta =
                usuarioService.atualizar(1L, request);

        assertEquals("Maria Souza", resposta.getNome());
        assertEquals("maria@email.com", resposta.getEmail());

        assertEquals(
                Perfil.PROFESSOR,
                resposta.getPerfil()
        );

        assertFalse(resposta.getAtivo());

        verify(usuarioRepository).findById(1L);

        verify(usuarioRepository)
                .findByEmailIgnoreCase(
                        "maria@email.com"
                );

        verify(passwordEncoder).encode("novaSenha");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveAtualizarUsuarioSemAlterarSenhaQuandoSenhaNaoForInformada() {
        Usuario usuario = criarUsuario();

        UsuarioUpdateRequest request =
                criarUsuarioUpdateRequest();

        request.setSenha(null);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.findByEmailIgnoreCase(
                "maria@email.com"
        )).thenReturn(Optional.empty());

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        UsuarioResponse resposta =
                usuarioService.atualizar(1L, request);

        assertEquals("Maria Souza", resposta.getNome());

        assertEquals(
                "senhaCriptografada",
                usuario.getSenha()
        );

        verify(passwordEncoder, never())
                .encode(any(String.class));
    }

    @Test
    void devePreservarAtivoESenhaQuandoNaoForemInformados() {
        Usuario usuario = criarUsuario();

        UsuarioUpdateRequest request =
                criarUsuarioUpdateRequest();

        request.setAtivo(null);
        request.setSenha("   ");

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.findByEmailIgnoreCase(
                "maria@email.com"
        )).thenReturn(Optional.empty());

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        UsuarioResponse resposta =
                usuarioService.atualizar(1L, request);

        assertTrue(resposta.getAtivo());

        assertEquals(
                "senhaCriptografada",
                usuario.getSenha()
        );

        verify(passwordEncoder, never())
                .encode(any(String.class));
    }

    @Test
    void devePermitirAtualizacaoMantendoEmailDoMesmoUsuario() {
        Usuario usuario = criarUsuario();

        UsuarioUpdateRequest request =
                criarUsuarioUpdateRequest();

        request.setEmail(" JOAO@EMAIL.COM ");
        request.setSenha(null);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.findByEmailIgnoreCase(
                "joao@email.com"
        )).thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        UsuarioResponse resposta =
                usuarioService.atualizar(1L, request);

        assertEquals(
                "joao@email.com",
                resposta.getEmail()
        );

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void deveLancarExcecaoAoAtualizarUsuarioInexistente() {
        UsuarioUpdateRequest request =
                criarUsuarioUpdateRequest();

        when(usuarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.atualizar(
                                999L,
                                request
                        )
                );

        assertEquals(
                "Usuário não encontrado com o ID: 999",
                excecao.getMessage()
        );

        verify(usuarioRepository).findById(999L);

        verify(usuarioRepository, never())
                .save(any(Usuario.class));
    }

    @Test
    void deveLancarExcecaoAoAtualizarComEmailDeOutroUsuario() {
        Usuario usuarioAtual = criarUsuario();

        Usuario outroUsuario = new Usuario();
        outroUsuario.setId(2L);
        outroUsuario.setEmail("maria@email.com");

        UsuarioUpdateRequest request =
                criarUsuarioUpdateRequest();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuarioAtual));

        when(usuarioRepository.findByEmailIgnoreCase(
                "maria@email.com"
        )).thenReturn(Optional.of(outroUsuario));

        IllegalArgumentException excecao =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> usuarioService.atualizar(
                                1L,
                                request
                        )
                );

        assertEquals(
                "Já existe um usuário cadastrado com este e-mail.",
                excecao.getMessage()
        );

        verify(usuarioRepository, never())
                .save(any(Usuario.class));
    }

    @Test
    void deveExcluirUsuarioComSucesso() {
        Usuario usuario = criarUsuario();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        usuarioService.excluir(1L);

        verify(usuarioRepository).findById(1L);
        verify(usuarioRepository).delete(usuario);
    }

    @Test
    void deveLancarExcecaoAoExcluirUsuarioInexistente() {
        when(usuarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException excecao =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> usuarioService.excluir(999L)
                );

        assertEquals(
                "Usuário não encontrado com o ID: 999",
                excecao.getMessage()
        );

        verify(usuarioRepository).findById(999L);

        verify(usuarioRepository, never())
                .delete(any(Usuario.class));
    }

    private Usuario criarUsuario() {
        Usuario usuario = new Usuario();

        usuario.setId(1L);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@email.com");
        usuario.setSenha("senhaCriptografada");
        usuario.setPerfil(Perfil.ALUNO);
        usuario.setAtivo(true);

        return usuario;
    }

    private UsuarioRequest criarUsuarioRequest() {
        UsuarioRequest request = new UsuarioRequest();

        request.setNome(" João Silva ");
        request.setEmail("JOAO@EMAIL.COM");
        request.setSenha("123456");
        request.setPerfil(Perfil.ALUNO);
        request.setAtivo(true);

        return request;
    }

    private UsuarioUpdateRequest criarUsuarioUpdateRequest() {
        UsuarioUpdateRequest request =
                new UsuarioUpdateRequest();

        request.setNome(" Maria Souza ");
        request.setEmail("MARIA@EMAIL.COM");
        request.setSenha("novaSenha");
        request.setPerfil(Perfil.PROFESSOR);
        request.setAtivo(false);

        return request;
    }
}