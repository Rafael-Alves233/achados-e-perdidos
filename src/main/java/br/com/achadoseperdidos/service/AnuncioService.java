package br.com.achadoseperdidos.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.achadoseperdidos.dto.AnuncioFormDto;
import br.com.achadoseperdidos.model.Anuncio;
import br.com.achadoseperdidos.model.Categoria;
import br.com.achadoseperdidos.model.StatusAnuncio;
import br.com.achadoseperdidos.model.Usuario;
import br.com.achadoseperdidos.repository.AnuncioRepository;
import br.com.achadoseperdidos.repository.CategoriaRepository;

/**
 * Servico responsavel pelas regras e operacoes relacionadas aos anuncios.
 */
@Service
public class AnuncioService {

    private final AnuncioRepository anuncioRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioService usuarioService;

    public AnuncioService(
            AnuncioRepository anuncioRepository,
            CategoriaRepository categoriaRepository,
            UsuarioService usuarioService) {
        this.anuncioRepository = anuncioRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioService = usuarioService;
    }

    /**
     * Lista os anuncios que ainda estao ativos no sistema.
     *
     * @return anuncios ativos ordenados pela data mais recente
     */
    @Transactional(readOnly = true)
    public List<Anuncio> listarAtivos() {
        return anuncioRepository.findByStatusOrderByDataDesc(StatusAnuncio.ATIVO);
    }

    /**
     * Lista as categorias disponiveis para cadastro de anuncios.
     *
     * @return categorias cadastradas no sistema
     */
    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    /**
     * Cadastra um novo anuncio ativo com base nos dados informados no formulario.
     *
     * @param form dados informados pelo usuario
     * @return anuncio salvo
     */
    @Transactional
    public Anuncio salvar(AnuncioFormDto form) {
        Categoria categoria = categoriaRepository.findById(form.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria nao encontrada."));
        Usuario usuario = usuarioService.obterOuCriarUsuarioPadrao();

        Anuncio anuncio = new Anuncio();
        anuncio.setTitulo(form.getTitulo().trim());
        anuncio.setDescricao(form.getDescricao().trim());
        anuncio.setTipoAnuncio(form.getTipoAnuncio());
        anuncio.setCategoria(categoria);
        anuncio.setLocal(form.getLocal().trim());
        anuncio.setData(LocalDate.now());
        anuncio.setStatus(StatusAnuncio.ATIVO);
        anuncio.setUsuario(usuario);

        return anuncioRepository.save(anuncio);
    }
}
