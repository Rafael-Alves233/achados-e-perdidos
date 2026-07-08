package br.com.achadoseperdidos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.achadoseperdidos.model.Anuncio;
import br.com.achadoseperdidos.model.StatusAnuncio;
import br.com.achadoseperdidos.model.TipoAnuncio;
import br.com.achadoseperdidos.model.Usuario;

/**
 * Repositorio responsavel pelas operacoes de persistencia de anuncios.
 */
public interface AnuncioRepository extends JpaRepository<Anuncio, Long> {

    /**
     * Busca um anuncio pelo id carregando dados usados na pagina de detalhes.
     *
     * @param id identificador do anuncio
     * @return anuncio encontrado, quando existir
     */
    @Query("""
            select a
            from Anuncio a
            join fetch a.categoria
            join fetch a.usuario
            where a.id = :id
            """)
    Optional<Anuncio> buscarPorIdComRelacionamentos(@Param("id") Long id);

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
     * Lista anuncios publicados por um usuario carregando dados usados nos templates.
     *
     * @param usuario usuario usado como filtro
     * @return lista de anuncios publicados pelo usuario
     */
    @Query("""
            select a
            from Anuncio a
            join fetch a.categoria
            join fetch a.usuario
            where a.usuario = :usuario
            order by a.data desc
            """)
    List<Anuncio> buscarPorUsuario(@Param("usuario") Usuario usuario);

    /**
     * Verifica se um usuario ja possui um anuncio com o mesmo titulo.
     *
     * @param titulo titulo usado na busca
     * @param usuario dono do anuncio
     * @return verdadeiro quando o anuncio ja existe
     */
    boolean existsByTituloAndUsuario(String titulo, Usuario usuario);

    /**
     * Conta quantos anuncios pertencem a um usuario.
     *
     * @param usuario dono dos anuncios
     * @return total de anuncios do usuario
     */
    long countByUsuario(Usuario usuario);

    /**
     * Conta quantos anuncios de um usuario possuem o status informado.
     *
     * @param usuario dono dos anuncios
     * @param status status usado como filtro
     * @return total de anuncios encontrados
     */
    long countByUsuarioAndStatus(Usuario usuario, StatusAnuncio status);

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
