package br.com.academiadigital.backend.usuario;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.academiadigital.backend.exception.ResourceNotFoundException;
import br.com.academiadigital.backend.usuario.dto.UsuarioRequest;
import br.com.academiadigital.backend.usuario.dto.UsuarioResponse;
import br.com.academiadigital.backend.usuario.dto.UsuarioUpdateRequest;
import br.com.academiadigital.backend.usuario.mapper.UsuarioMapper;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            BCryptPasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {

        

        String emailNormalizado = normalizarEmail(request.getEmail());

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new IllegalArgumentException(
                    "Já existe um usuário com este e-mail."
            );
        }

        Usuario usuario = UsuarioMapper.toEntity(request);

        usuario.setNome(request.getNome().trim());
        usuario.setEmail(emailNormalizado);

        if (usuario.getPerfil() == null) {
            usuario.setPerfil(Perfil.ALUNO);
        }

        if (usuario.getAtivo() == null) {
            usuario.setAtivo(true);
        }

        usuario.setSenha(passwordEncoder.encode(request.getSenha()));

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return UsuarioMapper.toResponse(usuarioSalvo);
    }

    @Transactional
public UsuarioResponse registrarAluno(
        String nome,
        String email,
        String senha) {

    String emailNormalizado = normalizarEmail(email);

    if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
        throw new IllegalArgumentException(
                "Já existe um usuário com este e-mail."
        );
    }

    Usuario usuario = new Usuario();

    usuario.setNome(nome.trim());
    usuario.setEmail(emailNormalizado);
    usuario.setSenha(passwordEncoder.encode(senha));
    usuario.setPerfil(Perfil.ALUNO);
    usuario.setAtivo(true);

    Usuario usuarioSalvo =
            usuarioRepository.save(usuario);

    return UsuarioMapper.toResponse(usuarioSalvo);
}

    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listarTodos(
            String nome,
            String email,
            String perfil,
            Boolean ativo,
            Pageable pageable) {

        Specification<Usuario> filtros =
                UsuarioSpecification.nomeContem(nome)
                        .and(UsuarioSpecification.emailContem(email))
                        .and(UsuarioSpecification.perfilIgual(perfil))
                        .and(UsuarioSpecification.ativoIgual(ativo));

        return usuarioRepository.findAll(filtros, pageable)
                .map(UsuarioMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioMapper.toResponse(buscarEntidadePorId(id));
    }

    @Transactional
    public UsuarioResponse atualizar(
            Long id,
            UsuarioUpdateRequest request) {

        Usuario usuario = buscarEntidadePorId(id);
        String emailNormalizado = normalizarEmail(request.getEmail());

        validarEmailDuplicadoNaAtualizacao(id, emailNormalizado);

        usuario.setNome(request.getNome().trim());
        usuario.setEmail(emailNormalizado);
        usuario.setPerfil(request.getPerfil());

        if (request.getAtivo() != null) {
            usuario.setAtivo(request.getAtivo());
        }

        if (request.getSenha() != null
                && !request.getSenha().isBlank()) {

            usuario.setSenha(
                    passwordEncoder.encode(request.getSenha())
            );
        }

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        return UsuarioMapper.toResponse(usuarioAtualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Usuário não encontrado com o ID: " + id
                        )
                );
    }

    private void validarEmailDuplicadoNaAtualizacao(
            Long id,
            String emailNormalizado) {

        usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .filter(usuarioEncontrado ->
                        !usuarioEncontrado.getId().equals(id))
                .ifPresent(usuarioEncontrado -> {
                    throw new IllegalArgumentException(
                            "Já existe um usuário cadastrado com este e-mail."
                    );
                });
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}