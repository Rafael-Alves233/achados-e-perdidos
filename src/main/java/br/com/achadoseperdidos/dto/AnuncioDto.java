package br.com.achadoseperdidos.dto;

import java.time.LocalDate;

import br.com.achadoseperdidos.model.StatusAnuncio;
import br.com.achadoseperdidos.model.TipoAnuncio;

/**
 * DTO utilizado para transportar dados resumidos de um anuncio entre camadas.
 *
 * @param id identificador do anuncio
 * @param titulo titulo do anuncio
 * @param descricao descricao do objeto perdido ou encontrado
 * @param tipoAnuncio tipo do anuncio
 * @param categoria nome da categoria do objeto
 * @param local local relacionado ao anuncio
 * @param data data do ocorrido ou da publicacao
 * @param imagem caminho ou identificador da imagem
 * @param status situacao atual do anuncio
 * @param usuario nome do usuario responsavel pelo anuncio
 */
public record AnuncioDto(
        Long id,
        String titulo,
        String descricao,
        TipoAnuncio tipoAnuncio,
        String categoria,
        String local,
        LocalDate data,
        String imagem,
        StatusAnuncio status,
        String usuario) {
}
