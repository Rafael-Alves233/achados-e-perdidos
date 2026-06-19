package br.com.achadoseperdidos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.achadoseperdidos.model.Usuario;

/**
 * Repositorio responsavel pelas operacoes de persistencia de usuarios.
 */
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca um usuario pelo endereco de e-mail.
     *
     * @param email e-mail utilizado na busca
     * @return usuario encontrado, quando existir
     */
    Optional<Usuario> findByEmail(String email);
}
