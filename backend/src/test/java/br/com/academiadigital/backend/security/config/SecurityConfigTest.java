package br.com.academiadigital.backend.security.config;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import br.com.academiadigital.backend.aula.AulaController;
import br.com.academiadigital.backend.aula.AulaService;
import br.com.academiadigital.backend.aula.StatusAula;
import br.com.academiadigital.backend.aula.dto.AulaRequest;
import br.com.academiadigital.backend.aula.dto.AulaResponse;
import br.com.academiadigital.backend.aula.dto.AulaUpdateRequest;
import br.com.academiadigital.backend.curso.CursoController;
import br.com.academiadigital.backend.curso.CursoService;
import br.com.academiadigital.backend.curso.NivelCurso;
import br.com.academiadigital.backend.curso.StatusCurso;
import br.com.academiadigital.backend.curso.dto.CursoRequest;
import br.com.academiadigital.backend.curso.dto.CursoResponse;
import br.com.academiadigital.backend.curso.dto.CursoUpdateRequest;
import br.com.academiadigital.backend.matricula.MatriculaController;
import br.com.academiadigital.backend.matricula.MatriculaService;
import br.com.academiadigital.backend.matricula.dto.MatriculaResponse;
import br.com.academiadigital.backend.progresso.ProgressoController;
import br.com.academiadigital.backend.progresso.ProgressoService;
import br.com.academiadigital.backend.progresso.dto.ProgressoAulaResponse;
import br.com.academiadigital.backend.progresso.dto.ProgressoCursoResponse;
import br.com.academiadigital.backend.trilha.TrilhaController;
import br.com.academiadigital.backend.trilha.TrilhaService;
import br.com.academiadigital.backend.trilha.dto.TrilhaRequest;
import br.com.academiadigital.backend.trilha.dto.TrilhaResponse;
import br.com.academiadigital.backend.security.UsuarioDetailsService;
import br.com.academiadigital.backend.security.auth.AuthController;
import br.com.academiadigital.backend.security.auth.AuthService;
import br.com.academiadigital.backend.security.auth.dto.LoginRequest;
import br.com.academiadigital.backend.security.auth.dto.TokenResponse;
import br.com.academiadigital.backend.security.handler.JwtAccessDeniedHandler;
import br.com.academiadigital.backend.security.handler.JwtAuthenticationEntryPoint;
import br.com.academiadigital.backend.security.jwt.JwtAuthenticationFilter;
import br.com.academiadigital.backend.security.jwt.JwtService;
import br.com.academiadigital.backend.usuario.UsuarioController;
import br.com.academiadigital.backend.usuario.UsuarioService;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;

@WebMvcTest({
        AuthController.class,
        UsuarioController.class,
        CursoController.class,
        AulaController.class,
        MatriculaController.class,
        ProgressoController.class,
        TrilhaController.class
})
@AutoConfigureMockMvc
@ImportAutoConfiguration({
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UsuarioService usuarioService;

    @MockitoBean
    private CursoService cursoService;

    @MockitoBean
    private AulaService aulaService;

    @MockitoBean
    private MatriculaService matriculaService;

    @MockitoBean
    private ProgressoService progressoService;

    @MockitoBean
    private TrilhaService trilhaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioDetailsService usuarioDetailsService;

    @BeforeEach
    void configurar() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devePermitirLoginSemTokenJwtESemTokenCsrf()
            throws Exception {

        TokenResponse resposta = new TokenResponse(
                "access-token",
                "refresh-token",
                "Bearer",
                900L
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(resposta);

        mockMvc.perform(
                        post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "email": "aluno@email.com",
                                          "senha": "Senha123!"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("access-token"));
    }

    @Test
    void deveRetornar401AoAcessarUsuariosSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/usuarios")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/usuarios"));
    }

    @Test
    void deveRetornar403QuandoAlunoAcessarUsuarios()
            throws Exception {

        configurarTokenValido(
                "token-aluno",
                "aluno@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        get("/api/v1/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/usuarios"));
    }

    @Test
    void devePermitirQueAdminAcesseUsuarios()
            throws Exception {

        configurarTokenValido(
                "token-admin",
                "admin@email.com",
                "ADMIN"
        );

        when(usuarioService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<UsuarioResponse>empty());

        mockMvc.perform(
                        get("/api/v1/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void naoDeveCriarSessaoAoAutenticarComJwt()
            throws Exception {

        configurarTokenValido(
                "token-admin",
                "admin@email.com",
                "ADMIN"
        );

        when(usuarioService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<UsuarioResponse>empty());

        MvcResult resultado = mockMvc.perform(
                        get("/api/v1/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isOk())
                .andReturn();

        assertNull(resultado.getRequest().getSession(false));
    }

    @Test
    void deveDisponibilizarBCryptPasswordEncoder() {
        String senha = "Senha123!";
        String senhaCriptografada =
                passwordEncoder.encode(senha);

        assertTrue(
                passwordEncoder.matches(
                        senha,
                        senhaCriptografada
                )
        );
    }

    @Test
    void deveRetornar401AoAcessarCursosSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/cursos")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/cursos"));
    }

    @Test
    void devePermitirQueAlunoListeCursos()
            throws Exception {

        configurarTokenValido(
                "token-aluno",
                "aluno@email.com",
                "ALUNO"
        );

        when(cursoService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<CursoResponse>empty());

        mockMvc.perform(
                        get("/api/v1/cursos")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void devePermitirQueProfessorListeCursos()
            throws Exception {

        configurarTokenValido(
                "token-professor",
                "professor@email.com",
                "PROFESSOR"
        );

        when(cursoService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<CursoResponse>empty());

        mockMvc.perform(
                        get("/api/v1/cursos")
                                .header(
                                        "Authorization",
                                        "Bearer token-professor"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deveRetornar403QuandoAlunoTentarCriarCurso()
            throws Exception {

        configurarTokenValido(
                "token-aluno",
                "aluno@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        post("/api/v1/cursos")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonCursoValido())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"));
    }

    @Test
    void deveRetornar403QuandoProfessorTentarAtualizarCurso()
            throws Exception {

        configurarTokenValido(
                "token-professor",
                "professor@email.com",
                "PROFESSOR"
        );

        mockMvc.perform(
                        put("/api/v1/cursos/{id}", 1L)
                                .header(
                                        "Authorization",
                                        "Bearer token-professor"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonCursoValido())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"));
    }

    @Test
    void deveRetornar403QuandoAlunoTentarExcluirCurso()
            throws Exception {

        configurarTokenValido(
                "token-aluno",
                "aluno@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        delete("/api/v1/cursos/{id}", 1L)
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"));
    }

    @Test
    void devePermitirQueAdminCrieCurso()
            throws Exception {

        configurarTokenValido(
                "token-admin",
                "admin@email.com",
                "ADMIN"
        );

        CursoResponse response = criarCursoResponse(
                "Java para iniciantes"
        );

        when(cursoService.criar(any(CursoRequest.class)))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/cursos")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonCursoValido())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo")
                        .value("Java para iniciantes"));
    }

    @Test
    void devePermitirQueAdminAtualizeCurso()
            throws Exception {

        configurarTokenValido(
                "token-admin",
                "admin@email.com",
                "ADMIN"
        );

        CursoResponse response = criarCursoResponse(
                "Java atualizado"
        );

        when(cursoService.atualizar(
                eq(1L),
                any(CursoUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/v1/cursos/{id}", 1L)
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "titulo": "Java atualizado",
                                          "descricao": "Curso atualizado.",
                                          "cargaHoraria": 60,
                                          "nivel": "INTERMEDIARIO",
                                          "status": "PUBLICADO"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo")
                        .value("Java atualizado"));
    }

    @Test
    void devePermitirQueAdminExcluaCurso()
            throws Exception {

        configurarTokenValido(
                "token-admin",
                "admin@email.com",
                "ADMIN"
        );

        doNothing()
                .when(cursoService)
                .excluir(1L);

        mockMvc.perform(
                        delete("/api/v1/cursos/{id}", 1L)
                                .header(
                                        "Authorization",
                                        "Bearer token-admin"
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar401AoAcessarAulasSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/aulas")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/aulas"));
    }

    @Test
    void devePermitirQueAlunoListeAulas()
            throws Exception {

        configurarTokenValido(
                "token-aluno-aulas",
                "aluno.aulas@email.com",
                "ALUNO"
        );

        when(aulaService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<AulaResponse>empty());

        mockMvc.perform(
                        get("/api/v1/aulas")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-aulas"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void devePermitirQueProfessorListeAulas()
            throws Exception {

        configurarTokenValido(
                "token-professor-aulas",
                "professor.aulas@email.com",
                "PROFESSOR"
        );

        when(aulaService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<AulaResponse>empty());

        mockMvc.perform(
                        get("/api/v1/aulas")
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-aulas"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void devePermitirQueAdminListeAulas()
            throws Exception {

        configurarTokenValido(
                "token-admin-aulas",
                "admin.aulas@email.com",
                "ADMIN"
        );

        when(aulaService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<AulaResponse>empty());

        mockMvc.perform(
                        get("/api/v1/aulas")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-aulas"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deveRetornar403QuandoAlunoTentarAlterarAulas()
            throws Exception {

        configurarTokenValido(
                "token-aluno-alteracao-aulas",
                "aluno.alteracao@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        post("/api/v1/aulas")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-alteracao-aulas"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonAulaValida())
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"));

        mockMvc.perform(
                        put("/api/v1/aulas/{id}", 10L)
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-alteracao-aulas"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonAulaValida())
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        delete("/api/v1/aulas/{id}", 10L)
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-alteracao-aulas"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirQueProfessorGerencieAulas()
            throws Exception {

        configurarTokenValido(
                "token-professor-gerencia-aulas",
                "professor.gerencia@email.com",
                "PROFESSOR"
        );

        AulaResponse aulaCriada = criarAulaResponse(
                10L,
                "Introdução ao computador",
                1
        );

        AulaResponse aulaAtualizada = criarAulaResponse(
                10L,
                "Aula atualizada",
                2
        );

        when(aulaService.criar(
                any(AulaRequest.class)
        )).thenReturn(aulaCriada);

        when(aulaService.atualizar(
                eq(10L),
                any(AulaUpdateRequest.class)
        )).thenReturn(aulaAtualizada);

        doNothing()
                .when(aulaService)
                .excluir(10L);

        mockMvc.perform(
                        post("/api/v1/aulas")
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-gerencia-aulas"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonAulaValida())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        mockMvc.perform(
                        put("/api/v1/aulas/{id}", 10L)
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-gerencia-aulas"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonAulaValida())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo")
                        .value("Aula atualizada"));

        mockMvc.perform(
                        delete("/api/v1/aulas/{id}", 10L)
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-gerencia-aulas"
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void devePermitirQueAdminGerencieAulas()
            throws Exception {

        configurarTokenValido(
                "token-admin-gerencia-aulas",
                "admin.gerencia@email.com",
                "ADMIN"
        );

        AulaResponse aulaCriada = criarAulaResponse(
                11L,
                "Conceitos de informática",
                1
        );

        AulaResponse aulaAtualizada = criarAulaResponse(
                11L,
                "Conceitos atualizados",
                2
        );

        when(aulaService.criar(
                any(AulaRequest.class)
        )).thenReturn(aulaCriada);

        when(aulaService.atualizar(
                eq(11L),
                any(AulaUpdateRequest.class)
        )).thenReturn(aulaAtualizada);

        doNothing()
                .when(aulaService)
                .excluir(11L);

        mockMvc.perform(
                        post("/api/v1/aulas")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-gerencia-aulas"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonAulaValida())
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(11));

        mockMvc.perform(
                        put("/api/v1/aulas/{id}", 11L)
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-gerencia-aulas"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonAulaValida())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo")
                        .value("Conceitos atualizados"));

        mockMvc.perform(
                        delete("/api/v1/aulas/{id}", 11L)
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-gerencia-aulas"
                                )
                )
                .andExpect(status().isNoContent());
    }

    private AulaResponse criarAulaResponse(
            Long id,
            String titulo,
            Integer ordem) {

        AulaResponse response = new AulaResponse();

        response.setId(id);
        response.setCursoId(1L);
        response.setCursoTitulo("Informática Básica");
        response.setTitulo(titulo);
        response.setDescricao(
                "Conhecendo os conceitos básicos de informática."
        );
        response.setOrdem(ordem);
        response.setDuracaoMinutos(30);
        response.setVideoUrl(
                "https://exemplo.com/videos/aula"
        );
        response.setStatus(StatusAula.PUBLICADA);

        return response;
    }

    private String jsonAulaValida() {
        return """
                {
                  "cursoId": 1,
                  "titulo": "Introdução ao computador",
                  "descricao": "Conhecendo os componentes do computador.",
                  "ordem": 1,
                  "duracaoMinutos": 30,
                  "videoUrl": "https://exemplo.com/videos/aula-1",
                  "status": "PUBLICADA"
                }
                """;
    }
    @Test
    void deveRetornar401AoAcessarMatriculasSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/matriculas")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/matriculas"));
    }

    @Test
    void deveRetornar403QuandoAlunoAcessarMatriculas()
            throws Exception {

        configurarTokenValido(
                "token-aluno-matriculas",
                "aluno.matriculas@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        get("/api/v1/matriculas")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-matriculas"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar403QuandoProfessorAcessarMatriculas()
            throws Exception {

        configurarTokenValido(
                "token-professor-matriculas",
                "professor.matriculas@email.com",
                "PROFESSOR"
        );

        mockMvc.perform(
                        get("/api/v1/matriculas")
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-matriculas"
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirQueAdminListeMatriculas()
            throws Exception {

        configurarTokenValido(
                "token-admin-matriculas",
                "admin.matriculas@email.com",
                "ADMIN"
        );

        when(matriculaService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(Page.<MatriculaResponse>empty());

        mockMvc.perform(
                        get("/api/v1/matriculas")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-matriculas"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deveRetornar401AoAcessarTrilhasSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/trilhas")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/trilhas"));
    }

    @Test
    void devePermitirQueAlunoListeTrilhas()
            throws Exception {

        configurarTokenValido(
                "token-aluno-trilhas",
                "aluno.trilhas@email.com",
                "ALUNO"
        );

        when(trilhaService.listarTodos(
                isNull(),
                isNull(),
                any()
        )).thenReturn(
                Page.<TrilhaResponse>empty()
        );

        mockMvc.perform(
                        get("/api/v1/trilhas")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-trilhas"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .isArray());
    }

    @Test
    void devePermitirQueProfessorListeTrilhas()
            throws Exception {

        configurarTokenValido(
                "token-professor-trilhas",
                "professor.trilhas@email.com",
                "PROFESSOR"
        );

        when(trilhaService.listarTodos(
                isNull(),
                isNull(),
                any()
        )).thenReturn(
                Page.<TrilhaResponse>empty()
        );

        mockMvc.perform(
                        get("/api/v1/trilhas")
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-trilhas"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .isArray());
    }

    @Test
    void deveRetornar403QuandoAlunoTentarCriarTrilha()
            throws Exception {

        configurarTokenValido(
                "token-aluno-criar-trilha",
                "aluno.criar.trilha@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        post("/api/v1/trilhas")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-criar-trilha"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "titulo": "Formação em Java",
                                          "descricao": "Trilha de Java.",
                                          "status": "RASCUNHO"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());

        verify(
                trilhaService,
                never()
        ).criar(any(TrilhaRequest.class));
    }

    @Test
    void deveRetornar403QuandoProfessorTentarCriarTrilha()
            throws Exception {

        configurarTokenValido(
                "token-professor-criar-trilha",
                "professor.criar.trilha@email.com",
                "PROFESSOR"
        );

        mockMvc.perform(
                        post("/api/v1/trilhas")
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-criar-trilha"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "titulo": "Formação em Java",
                                          "descricao": "Trilha de Java.",
                                          "status": "RASCUNHO"
                                        }
                                        """)
                )
                .andExpect(status().isForbidden());

        verify(
                trilhaService,
                never()
        ).criar(any(TrilhaRequest.class));
    }

    @Test
    void devePermitirQueAdminCrieTrilha()
            throws Exception {

        configurarTokenValido(
                "token-admin-criar-trilha",
                "admin.criar.trilha@email.com",
                "ADMIN"
        );

        TrilhaResponse response =
                new TrilhaResponse();

        response.setId(1L);
        response.setTitulo(
                "Formação em Java"
        );
        response.setDescricao(
                "Trilha de Java."
        );

        when(trilhaService.criar(
                any(TrilhaRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/trilhas")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-criar-trilha"
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "titulo": "Formação em Java",
                                          "descricao": "Trilha de Java.",
                                          "status": "RASCUNHO"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(1))
                .andExpect(jsonPath("$.titulo")
                        .value("Formação em Java"));

        verify(trilhaService).criar(
                any(TrilhaRequest.class)
        );
    }

    @Test
    void deveRetornar401AoAcessarMinhasMatriculasSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/matriculas/minhas")
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value("/api/v1/matriculas/minhas"));
    }

    @Test
    void devePermitirQueAlunoListeSuasMatriculas()
            throws Exception {

        configurarTokenValido(
                "token-aluno-minhas-matriculas",
                "aluno.matriculas@email.com",
                "ALUNO"
        );

        when(matriculaService.listarMinhas(
                eq("aluno.matriculas@email.com"),
                any()
        )).thenReturn(
                Page.<MatriculaResponse>empty()
        );

        mockMvc.perform(
                        get("/api/v1/matriculas/minhas")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-minhas-matriculas"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .isArray());

        verify(matriculaService).listarMinhas(
                eq("aluno.matriculas@email.com"),
                any()
        );
    }

    @Test
    void deveRetornar403QuandoProfessorAcessarMinhasMatriculas()
            throws Exception {

        configurarTokenValido(
                "token-professor-minhas-matriculas",
                "professor.matriculas@email.com",
                "PROFESSOR"
        );

        mockMvc.perform(
                        get("/api/v1/matriculas/minhas")
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-minhas-matriculas"
                                )
                )
                .andExpect(status().isForbidden());

        verify(
                matriculaService,
                never()
        ).listarMinhas(
                any(),
                any()
        );
    }

    @Test
    void deveRetornar403QuandoAlunoListarTodasAsMatriculas()
            throws Exception {

        configurarTokenValido(
                "token-aluno-todas-matriculas",
                "aluno.todas.matriculas@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        get("/api/v1/matriculas")
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-todas-matriculas"
                                )
                )
                .andExpect(status().isForbidden());

        verify(
                matriculaService,
                never()
        ).listarTodos(
                any(),
                any(),
                any(),
                any()
        );
    }

    @Test
    void devePermitirQueAdminListeTodasAsMatriculas()
            throws Exception {

        configurarTokenValido(
                "token-admin-todas-matriculas",
                "admin.matriculas@email.com",
                "ADMIN"
        );

        when(matriculaService.listarTodos(
                isNull(),
                isNull(),
                isNull(),
                any()
        )).thenReturn(
                Page.<MatriculaResponse>empty()
        );

        mockMvc.perform(
                        get("/api/v1/matriculas")
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-todas-matriculas"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content")
                        .isArray());

        verify(matriculaService).listarTodos(
                isNull(),
                isNull(),
                isNull(),
                any()
        );
    }

    @Test
    void deveRetornar401AoAcessarProgressoSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/v1/progressos/cursos/{cursoId}",
                                10L
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value(
                                "/api/v1/progressos/cursos/10"
                        ));
    }

    @Test
    void devePermitirQueAlunoConsulteProgressoDoCurso()
            throws Exception {

        configurarTokenValido(
                "token-aluno-progresso",
                "aluno.progresso@email.com",
                "ALUNO"
        );

        ProgressoCursoResponse response =
                new ProgressoCursoResponse();

        response.setMatriculaId(20L);
        response.setCursoId(10L);
        response.setCursoTitulo("Informática Básica");
        response.setTotalAulas(2);
        response.setAulasConcluidas(1);
        response.setPercentualConclusao(50.0);

        when(progressoService.buscarProgressoCurso(
                "aluno.progresso@email.com",
                10L
        )).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/progressos/cursos/{cursoId}",
                                10L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-progresso"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaId")
                        .value(20))
                .andExpect(jsonPath("$.cursoId")
                        .value(10))
                .andExpect(jsonPath("$.cursoTitulo")
                        .value("Informática Básica"))
                .andExpect(jsonPath("$.totalAulas")
                        .value(2))
                .andExpect(jsonPath("$.aulasConcluidas")
                        .value(1))
                .andExpect(jsonPath("$.percentualConclusao")
                        .value(50.0));

        verify(progressoService)
                .buscarProgressoCurso(
                        "aluno.progresso@email.com",
                        10L
                );
    }

    @Test
    void deveRetornar403QuandoProfessorConsultarProgresso()
            throws Exception {

        configurarTokenValido(
                "token-professor-progresso",
                "professor.progresso@email.com",
                "PROFESSOR"
        );

        mockMvc.perform(
                        get(
                                "/api/v1/progressos/cursos/{cursoId}",
                                10L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-progresso"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status")
                        .value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"));

        verify(
                progressoService,
                never()
        ).buscarProgressoCurso(
                any(),
                any()
        );
    }

    @Test
    void deveRetornar401AoConcluirAulaSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/v1/progressos/aulas/{aulaId}/concluir",
                                30L
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value(
                                "/api/v1/progressos/aulas/30/concluir"
                        ));

        verify(
                progressoService,
                never()
        ).concluirAula(
                any(),
                any()
        );
    }

    @Test
    void devePermitirQueAlunoConcluaAula()
            throws Exception {

        configurarTokenValido(
                "token-aluno-concluir-aula",
                "aluno.conclusao@email.com",
                "ALUNO"
        );

        ProgressoAulaResponse response =
                new ProgressoAulaResponse();

        response.setId(40L);
        response.setMatriculaId(20L);
        response.setCursoId(10L);
        response.setAulaId(30L);
        response.setConcluida(true);

        when(
                progressoService.concluirAula(
                        "aluno.conclusao@email.com",
                        30L
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post(
                                "/api/v1/progressos/aulas/{aulaId}/concluir",
                                30L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-concluir-aula"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(40))
                .andExpect(jsonPath("$.matriculaId")
                        .value(20))
                .andExpect(jsonPath("$.cursoId")
                        .value(10))
                .andExpect(jsonPath("$.aulaId")
                        .value(30))
                .andExpect(jsonPath("$.concluida")
                        .value(true));

        verify(progressoService)
                .concluirAula(
                        "aluno.conclusao@email.com",
                        30L
                );
    }

    @Test
    void deveRetornar403QuandoProfessorTentarConcluirAula()
            throws Exception {

        configurarTokenValido(
                "token-professor-concluir-aula",
                "professor.conclusao@email.com",
                "PROFESSOR"
        );

        mockMvc.perform(
                        post(
                                "/api/v1/progressos/aulas/{aulaId}/concluir",
                                30L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-concluir-aula"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status")
                        .value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"))
                .andExpect(jsonPath("$.caminho")
                        .value(
                                "/api/v1/progressos/aulas/30/concluir"
                        ));

        verify(
                progressoService,
                never()
        ).concluirAula(
                any(),
                any()
        );
    }

    @Test
    void deveRetornar403QuandoAdminTentarConcluirAula()
            throws Exception {

        configurarTokenValido(
                "token-admin-concluir-aula",
                "admin.conclusao@email.com",
                "ADMIN"
        );

        mockMvc.perform(
                        post(
                                "/api/v1/progressos/aulas/{aulaId}/concluir",
                                30L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-concluir-aula"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status")
                        .value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"))
                .andExpect(jsonPath("$.caminho")
                        .value(
                                "/api/v1/progressos/aulas/30/concluir"
                        ));

        verify(
                progressoService,
                never()
        ).concluirAula(
                any(),
                any()
        );
    }

    @Test
    void deveRetornar401AoRemoverConclusaoSemAutenticacao()
            throws Exception {

        mockMvc.perform(
                        delete(
                                "/api/v1/progressos/aulas/{aulaId}/conclusao",
                                30L
                        )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$.status")
                        .value(401))
                .andExpect(jsonPath("$.erro")
                        .value("Não autorizado"))
                .andExpect(jsonPath("$.caminho")
                        .value(
                                "/api/v1/progressos/aulas/30/conclusao"
                        ));

        verify(
                progressoService,
                never()
        ).removerConclusaoAula(
                any(),
                any()
        );
    }

    @Test
    void devePermitirQueAlunoRemovaConclusaoDaAula()
            throws Exception {

        configurarTokenValido(
                "token-aluno-remover-conclusao",
                "aluno.remocao@email.com",
                "ALUNO"
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/progressos/aulas/{aulaId}/conclusao",
                                30L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-aluno-remover-conclusao"
                                )
                )
                .andExpect(status().isNoContent());

        verify(progressoService)
                .removerConclusaoAula(
                        "aluno.remocao@email.com",
                        30L
                );
    }

    @Test
    void deveRetornar403QuandoProfessorTentarRemoverConclusao()
            throws Exception {

        configurarTokenValido(
                "token-professor-remover-conclusao",
                "professor.remocao@email.com",
                "PROFESSOR"
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/progressos/aulas/{aulaId}/conclusao",
                                30L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-professor-remover-conclusao"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status")
                        .value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"))
                .andExpect(jsonPath("$.caminho")
                        .value(
                                "/api/v1/progressos/aulas/30/conclusao"
                        ));

        verify(
                progressoService,
                never()
        ).removerConclusaoAula(
                any(),
                any()
        );
    }

    @Test
    void deveRetornar403QuandoAdminTentarRemoverConclusao()
            throws Exception {

        configurarTokenValido(
                "token-admin-remover-conclusao",
                "admin.remocao@email.com",
                "ADMIN"
        );

        mockMvc.perform(
                        delete(
                                "/api/v1/progressos/aulas/{aulaId}/conclusao",
                                30L
                        )
                                .header(
                                        "Authorization",
                                        "Bearer token-admin-remover-conclusao"
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status")
                        .value(403))
                .andExpect(jsonPath("$.erro")
                        .value("Acesso negado"))
                .andExpect(jsonPath("$.caminho")
                        .value(
                                "/api/v1/progressos/aulas/30/conclusao"
                        ));

        verify(
                progressoService,
                never()
        ).removerConclusaoAula(
                any(),
                any()
        );
    }

    private CursoResponse criarCursoResponse(String titulo) {
        CursoResponse response = new CursoResponse();
        response.setId(1L);
        response.setTitulo(titulo);
        response.setDescricao("Curso introdutório de Java.");
        response.setCargaHoraria(40);
        response.setNivel(NivelCurso.INICIANTE);
        response.setStatus(StatusCurso.PUBLICADO);
        return response;
    }

    private String jsonCursoValido() {
        return """
                {
                  "titulo": "Java para iniciantes",
                  "descricao": "Curso introdutório de Java.",
                  "cargaHoraria": 40,
                  "nivel": "INICIANTE",
                  "status": "PUBLICADO"
                }
                """;
    }

    private void configurarTokenValido(
            String token,
            String email,
            String perfil) {

        UserDetails userDetails = User.builder()
                .username(email)
                .password("senhaCriptografada")
                .roles(perfil)
                .build();

        when(jwtService.extrairEmail(token))
                .thenReturn(email);

        when(usuarioDetailsService.loadUserByUsername(email))
                .thenReturn(userDetails);

        when(jwtService.accessTokenValido(
                token,
                userDetails
        )).thenReturn(true);
    }
}
