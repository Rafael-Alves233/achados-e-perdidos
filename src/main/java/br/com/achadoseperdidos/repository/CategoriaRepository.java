package br.com.achadoseperdidos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.achadoseperdidos.model.Categoria;

/**
 * Repositorio responsavel pelas operacoes de persistencia de categorias.
 */
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Busca uma categoria pelo nome.
     *
     * @param nome nome da categoria
     * @return categoria encontrada, quando existir
     */
    Optional<Categoria> findByNome(String nome);
}
