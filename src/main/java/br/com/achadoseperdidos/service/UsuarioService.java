package br.com.achadoseperdidos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Busca ou cria o usuario padrao usado enquanto o sistema ainda nao possui
     * autenticacao.
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
                    usuario.setSenha("123456");
                    usuario.setTipoUsuario(TipoUsuario.ADMIN);
                    return usuarioRepository.save(usuario);
                });
    }
}
