package br.com.academiadigital.backend.usuario;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtAuthenticationFilter;
import br.com.academiadigital.backend.usuario.dto.UsuarioRequest;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;
import br.com.academiadigital.backend.usuario.dto.UsuarioUpdateRequest;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Test
    void deveBuscarUsuarioPorId() throws Exception {

        UsuarioResponse response = new UsuarioResponse();

        response.setId(1L);
        response.setNome("João Silva");
        response.setEmail("joao@email.com");
        response.setPerfil(Perfil.ALUNO);
        response.setAtivo(true);

        when(usuarioService.buscarPorId(1L))
                .thenReturn(response);

        mockMvc.perform(
                get("/api/v1/usuarios/{id}", 1L)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.nome").value("João Silva"))
        .andExpect(jsonPath("$.email").value("joao@email.com"))
        .andExpect(jsonPath("$.perfil").value("ALUNO"))
        .andExpect(jsonPath("$.ativo").value(true));

        verify(usuarioService).buscarPorId(1L);
    }

    @Test
    void deveExcluirUsuario() throws Exception {

        doNothing()
                .when(usuarioService)
                .excluir(1L);

        mockMvc.perform(
                delete("/api/v1/usuarios/{id}", 1L)
        )
        .andExpect(status().isNoContent());

        verify(usuarioService).excluir(1L);
    }


    @Test
void deveCriarUsuario() throws Exception {

    UsuarioRequest request = new UsuarioRequest();
    request.setNome("Maria Silva");
    request.setEmail("maria@email.com");
    request.setSenha("senha123");
    request.setPerfil(Perfil.ALUNO);
    request.setAtivo(true);

    UsuarioResponse response = new UsuarioResponse();
    response.setId(1L);
    response.setNome("Maria Silva");
    response.setEmail("maria@email.com");
    response.setPerfil(Perfil.ALUNO);
    response.setAtivo(true);

    when(usuarioService.criar(any(UsuarioRequest.class)))
            .thenReturn(response);

    mockMvc.perform(
            post("/api/v1/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isCreated())
    .andExpect(header().string(
            "Location",
            "http://localhost/api/v1/usuarios/1"
    ))
    .andExpect(jsonPath("$.id").value(1))
    .andExpect(jsonPath("$.nome").value("Maria Silva"))
    .andExpect(jsonPath("$.email").value("maria@email.com"))
    .andExpect(jsonPath("$.perfil").value("ALUNO"))
    .andExpect(jsonPath("$.ativo").value(true));

    verify(usuarioService).criar(any(UsuarioRequest.class));
}

@Test
void deveAtualizarUsuario() throws Exception {

    UsuarioUpdateRequest request = new UsuarioUpdateRequest();
    request.setNome("Maria Souza");
    request.setEmail("maria.souza@email.com");
    request.setSenha("novaSenha123");
    request.setPerfil(Perfil.PROFESSOR);
    request.setAtivo(true);

    UsuarioResponse response = new UsuarioResponse();
    response.setId(1L);
    response.setNome("Maria Souza");
    response.setEmail("maria.souza@email.com");
    response.setPerfil(Perfil.PROFESSOR);
    response.setAtivo(true);

    when(usuarioService.atualizar(
        eq(1L),
        any(UsuarioUpdateRequest.class)
)).thenReturn(response);

    mockMvc.perform(
            put("/api/v1/usuarios/{id}", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.id").value(1))
    .andExpect(jsonPath("$.nome").value("Maria Souza"))
    .andExpect(jsonPath("$.email").value("maria.souza@email.com"))
    .andExpect(jsonPath("$.perfil").value("PROFESSOR"))
    .andExpect(jsonPath("$.ativo").value(true));

    verify(usuarioService).atualizar(
        eq(1L),
        any(UsuarioUpdateRequest.class)
    );
}

@Test
void deveListarUsuariosComPaginacao() throws Exception {

    UsuarioResponse usuario1 = new UsuarioResponse();
    usuario1.setId(1L);
    usuario1.setNome("Maria Silva");
    usuario1.setEmail("maria@email.com");
    usuario1.setPerfil(Perfil.ALUNO);
    usuario1.setAtivo(true);

    UsuarioResponse usuario2 = new UsuarioResponse();
    usuario2.setId(2L);
    usuario2.setNome("João Souza");
    usuario2.setEmail("joao@email.com");
    usuario2.setPerfil(Perfil.PROFESSOR);
    usuario2.setAtivo(true);

    Page<UsuarioResponse> pagina = new PageImpl<>(
            List.of(usuario1, usuario2),
            PageRequest.of(0, 10),
            2
    );

    when(usuarioService.listarTodos(
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            any()
    )).thenReturn(pagina);

    mockMvc.perform(
            get("/api/v1/usuarios")
                    .param("page", "0")
                    .param("size", "10")
    )
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.content").isArray())
    .andExpect(jsonPath("$.content.length()").value(2))
    .andExpect(jsonPath("$.content[0].id").value(1))
    .andExpect(jsonPath("$.content[0].nome").value("Maria Silva"))
    .andExpect(jsonPath("$.content[1].id").value(2))
    .andExpect(jsonPath("$.content[1].nome").value("João Souza"))
    .andExpect(jsonPath("$.totalElements").value(2))
    .andExpect(jsonPath("$.number").value(0))
    .andExpect(jsonPath("$.size").value(10));

    verify(usuarioService).listarTodos(
            isNull(),
            isNull(),
            isNull(),
            isNull(),
            any()
    );
}

@Test
void deveRetornarBadRequestAoCriarUsuarioComDadosInvalidos() throws Exception {

    UsuarioRequest request = new UsuarioRequest();
    request.setNome("");
    request.setEmail("email-invalido");
    request.setSenha("123");
    request.setPerfil(null);
    request.setAtivo(true);

    mockMvc.perform(
            post("/api/v1/usuarios")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
    )
    .andExpect(status().isBadRequest());

    verify(usuarioService, never())
            .criar(any(UsuarioRequest.class));
}

@Test
void deveRetornarNotFoundAoBuscarUsuarioInexistente() throws Exception {

    when(usuarioService.buscarPorId(99L))
            .thenThrow(new ResourceNotFoundException(
                    "Usuário não encontrado com o ID: 99"
            ));

    mockMvc.perform(
            get("/api/v1/usuarios/{id}", 99L)
    )
    .andExpect(status().isNotFound())
    .andExpect(jsonPath("$.status").value(404))
    .andExpect(jsonPath("$.error").value("Not Found"))
    .andExpect(jsonPath("$.message")
            .value("Usuário não encontrado com o ID: 99"))
    .andExpect(jsonPath("$.path")
            .value("/api/v1/usuarios/99"));

    verify(usuarioService).buscarPorId(99L);
}
}