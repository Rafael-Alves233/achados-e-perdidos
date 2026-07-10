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

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public long getTotalAnuncios() {
        return totalAnuncios;
    }

    public long getAnunciosAtivos() {
        return anunciosAtivos;
    }

    public long getAnunciosResolvidos() {
        return anunciosResolvidos;
    }

    public long getAnunciosPerdidos() {
        return anunciosPerdidos;
    }

    public long getAnunciosEncontrados() {
        return anunciosEncontrados;
    }

    public List<Anuncio> getAnuncios() {
        return anuncios;
    }
}
