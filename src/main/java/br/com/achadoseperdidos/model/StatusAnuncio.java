package br.com.achadoseperdidos.model;

/**
 * Define a situacao atual de um anuncio.
 */
public enum StatusAnuncio {
    /**
     * Anuncio ainda disponivel para consulta.
     */
    ATIVO,

    /**
     * Anuncio encerrado porque o objeto foi devolvido ou resolvido.
     */
    RESOLVIDO
}
