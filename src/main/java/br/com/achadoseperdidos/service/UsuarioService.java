package br.com.achadoseperdidos.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.achadoseperdidos.dto.UsuarioCadastroDto;
import br.com.achadoseperdidos.dto.UsuarioPerfilDto;
import br.com.achadoseperdidos.model.TipoUsuario;
import br.com.achadoseperdidos.model.Usuario;
import br.com.achadoseperdidos.repository.UsuarioRepository;

/**
 * Servico responsavel pelas operacoes relacionadas aos usuarios do sistema.
 */
@Service
public class UsuarioService {

    private static final String EMAIL_USUARIO_PADRAO = "secretaria@faculdade.edu";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Busca ou cria o usuario padrao usado para demonstracao e administracao.
     *
     * @return usuario padrao do sistema
     */
    @Transactional
    public Usuario obterOuCriarUsuarioPadrao() {
        return usuarioRepository.findByEmail(EMAIL_USUARIO_PADRAO)
                .orElseGet(() -> {
                    Usuario usuario = new Usuario();
                    usuario.setNome("Secretaria Academica");
                    usuario.setEmail(EMAIL_USUARIO_PADRAO);
                    usuario.setSenha(passwordEncoder.encode("123456"));
                    usuario.setTipoUsuario(TipoUsuario.ADMIN);
                    return usuarioRepository.save(usuario);
                });
    }

    /**
     * Cadastra um usuario comum.
     *
     * @param form dados informados no formulario de cadastro
     * @return usuario cadastrado
     */
    @Transactional
    public Usuario cadastrar(UsuarioCadastroDto form) {
        String email = normalizarEmail(form.getEmail());

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("E-mail ja cadastrado.");
        }

        if (!form.getSenha().equals(form.getConfirmacaoSenha())) {
            throw new IllegalArgumentException("As senhas informadas nao conferem.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(form.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(form.getSenha()));
        usuario.setTipoUsuario(TipoUsuario.USUARIO);
        return usuarioRepository.save(usuario);
    }

    /**
     * Atualiza os dados editaveis do perfil do usuario autenticado.
     *
     * @param form dados informados no formulario de perfil
     * @return usuario atualizado
     */
    @Transactional
    public Usuario atualizarPerfil(UsuarioPerfilDto form) {
        Usuario usuario = obterUsuarioAutenticado();
        usuario.setNome(form.getNome().trim());
        return usuario;
    }

    /**
     * Busca o usuario autenticado no contexto de seguranca.
     *
     * @return usuario autenticado
     */
    @Transactional(readOnly = true)
    public Usuario obterUsuarioAutenticado() {
        return buscarUsuarioAutenticado()
                .orElseThrow(() -> new IllegalArgumentException("Usuario autenticado nao encontrado."));
    }

    /**
     * Busca o usuario autenticado quando houver login ativo.
     *
     * @return usuario autenticado, quando existir
     */
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioAutenticado() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        return usuarioRepository.findByEmail(normalizarEmail(authentication.getName()));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
