package br.com.achadoseperdidos.dto;

import br.com.achadoseperdidos.model.TipoAnuncio;

/**
 * DTO utilizado para transportar os filtros aplicados na listagem de anuncios.
 */
public class AnuncioFiltroDto {

    private String termo;
    private TipoAnuncio tipoAnuncio;
    private Long categoriaId;
    private String local;

    public String getTermo() {
        return termo;
    }

    public void setTermo(String termo) {
        this.termo = termo;
    }

    public TipoAnuncio getTipoAnuncio() {
        return tipoAnuncio;
    }

    public void setTipoAnuncio(TipoAnuncio tipoAnuncio) {
        this.tipoAnuncio = tipoAnuncio;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }
}
