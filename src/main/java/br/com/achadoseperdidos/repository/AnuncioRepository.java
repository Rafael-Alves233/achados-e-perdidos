package br.com.achadoseperdidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.achadoseperdidos.model.Anuncio;
import br.com.achadoseperdidos.model.StatusAnuncio;
import br.com.achadoseperdidos.model.TipoAnuncio;

/**
 * Repositorio responsavel pelas operacoes de persistencia de anuncios.
 */
public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {

    /**
     * Lista anuncios por status carregando dados usados nos templates.
     *
     * @param status status usado como filtro
     * @return lista de anuncios encontrados
     */
    @Query("""
            select a
            from Anuncio a
            join fetch a.categoria
            join fetch a.usuario
            where a.status = :status
            order by a.data desc
            """)
    List<Anuncio> buscarPorStatus(@Param("status") StatusAnuncio status);

    /**
     * Lista anuncios ativos aplicando filtros opcionais de busca.
     *
     * @param status status usado como filtro obrigatorio
     * @param termo texto buscado no titulo ou na descricao
     * @param tipoAnuncio tipo do anuncio
     * @param categoriaId identificador da categoria
     * @param local texto buscado no local
     * @return lista de anuncios encontrados
     */
    @Query("""
            select a
            from Anuncio a
            join fetch a.categoria
            join fetch a.usuario
            where a.status = :status
              and (:termo is null
                   or lower(a.titulo) like lower(concat('%', :termo, '%'))
                   or lower(a.descricao) like lower(concat('%', :termo, '%')))
              and (:tipoAnuncio is null or a.tipoAnuncio = :tipoAnuncio)
              and (:categoriaId is null or a.categoria.id = :categoriaId)
              and (:local is null or lower(a.local) like lower(concat('%', :local, '%')))
            order by a.data desc
            """)
    List<Anuncio> buscarAtivosComFiltros(
            @Param("status") StatusAnuncio status,
            @Param("termo") String termo,
            @Param("tipoAnuncio") TipoAnuncio tipoAnuncio,
            @Param("categoriaId") Long categoriaId,
            @Param("local") String local);
}
