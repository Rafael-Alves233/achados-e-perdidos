package br.com.achadoseperdidos.dto;

import br.com.achadoseperdidos.model.TipoAnuncio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO utilizado para receber os dados do formulario de cadastro de anuncios.
 */
public class AnuncioFormDto {

    @NotBlank(message = "Informe o titulo do anuncio.")
    @Size(max = 120, message = "O titulo deve ter no maximo 120 caracteres.")
    private String titulo;

    @NotBlank(message = "Informe a descricao do objeto.")
    @Size(max = 1000, message = "A descricao deve ter no maximo 1000 caracteres.")
    private String descricao;

    @NotNull(message = "Selecione o tipo do anuncio.")
    private TipoAnuncio tipoAnuncio;

    @NotNull(message = "Selecione uma categoria.")
    private Long categoriaId;

    @NotBlank(message = "Informe o local relacionado ao anuncio.")
    @Size(max = 120, message = "O local deve ter no maximo 120 caracteres.")
    private String local;

    private MultipartFile imagemArquivo;

    private String imagemAtual;

    private boolean removerImagem;

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public MultipartFile getImagemArquivo() {
        return imagemArquivo;
    }

    public void setImagemArquivo(MultipartFile imagemArquivo) {
        this.imagemArquivo = imagemArquivo;
    }

    public String getImagemAtual() {
        return imagemAtual;
    }

    public void setImagemAtual(String imagemAtual) {
        this.imagemAtual = imagemAtual;
    }

    public boolean isRemoverImagem() {
        return removerImagem;
    }

    public void setRemoverImagem(boolean removerImagem) {
        this.removerImagem = removerImagem;
    }
}
