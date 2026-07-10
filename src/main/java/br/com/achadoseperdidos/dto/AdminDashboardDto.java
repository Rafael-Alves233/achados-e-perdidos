package br.com.achadoseperdidos.dto;

import java.util.List;

import br.com.achadoseperdidos.model.Anuncio;

/**
 * Reune os dados exibidos no painel administrativo.
 */
public class AdminDashboardDto {

    private final long totalUsuarios;
    private final long totalAnuncios;
    private final long anunciosAtivos;
    private final long anunciosResolvidos;
    private final long anunciosPerdidos;
    private final long anunciosEncontrados;
    private final List<Anuncio> anuncios;

    /**
     * Cria um resumo imutavel dos indicadores exibidos no painel.
     *
     * @param totalUsuarios quantidade total de usuarios
     * @param totalAnuncios quantidade total de anuncios
     * @param anunciosAtivos quantidade de anuncios ativos
     * @param anunciosResolvidos quantidade de anuncios resolvidos
     * @param anunciosPerdidos quantidade de itens perdidos
     * @param anunciosEncontrados quantidade de itens encontrados
     * @param anuncios anuncios exibidos na listagem administrativa
     */
    public AdminDashboardDto(
            long totalUsuarios,
            long totalAnuncios,
            long anunciosAtivos,
            long anunciosResolvidos,
            long anunciosPerdidos,
            long anunciosEncontrados,
            List<Anuncio> anuncios) {
        this.totalUsuarios = totalUsuarios;
        this.totalAnuncios = totalAnuncios;
        this.anunciosAtivos = anunciosAtivos;
        this.anunciosResolvidos = anunciosResolvidos;
        this.anunciosPerdidos = anunciosPerdidos;
        this.anunciosEncontrados = anunciosEncontrados;
        this.anuncios = anuncios;
    }

    /**
     * @return quantidade total de usuarios cadastrados
     */
    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    /**
     * @return quantidade total de anuncios publicados
     */
    public long getTotalAnuncios() {
        return totalAnuncios;
    }

    /**
     * @return quantidade de anuncios ativos
     */
    public long getAnunciosAtivos() {
        return anunciosAtivos;
    }

    /**
     * @return quantidade de anuncios resolvidos
     */
    public long getAnunciosResolvidos() {
        return anunciosResolvidos;
    }

    /**
     * @return quantidade de anuncios de itens perdidos
     */
    public long getAnunciosPerdidos() {
        return anunciosPerdidos;
    }

    /**
     * @return quantidade de anuncios de itens encontrados
     */
    public long getAnunciosEncontrados() {
        return anunciosEncontrados;
    }

    /**
     * @return anuncios exibidos no painel administrativo
     */
    public List<Anuncio> getAnuncios() {
        return anuncios;
    }
}
