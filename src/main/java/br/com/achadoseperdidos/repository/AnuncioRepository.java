package br.com.achadoseperdidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.achadoseperdidos.model.Anuncio;
import br.com.achadoseperdidos.model.StatusAnuncio;

/**
 * Repositorio responsavel pelas operacoes de persistencia de anuncios.
 */
public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {

    /**
     * Lista anuncios filtrados por status, ordenando os mais recentes primeiro.
     *
     * @param status status usado como filtro
     * @return lista de anuncios encontrados
     */
    List<Anuncio> findByStatusOrderByDataDesc(StatusAnuncio status);
}
