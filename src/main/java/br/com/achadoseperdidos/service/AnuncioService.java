package br.com.achadoseperdidos.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.achadoseperdidos.dto.AnuncioFiltroDto;
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
    private final ImagemStorageService imagemStorageService;

    public AnuncioService(
            AnuncioRepository anuncioRepository,
            CategoriaRepository categoriaRepository,
            UsuarioService usuarioService,
            ImagemStorageService imagemStorageService) {
        this.anuncioRepository = anuncioRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioService = usuarioService;
        this.imagemStorageService = imagemStorageService;
    }

    /**
     * Lista os anuncios ativos aplicando filtros opcionais.
     *
     * @param filtro dados usados para filtrar a listagem
     * @return anuncios ativos que correspondem aos filtros informados
     */
    @Transactional(readOnly = true)
    public List<Anuncio> listarAtivos(AnuncioFiltroDto filtro) {
        return anuncioRepository.buscarAtivosComFiltros(
                StatusAnuncio.ATIVO,
                normalizarTexto(filtro.getTermo()),
                filtro.getTipoAnuncio(),
                filtro.getCategoriaId(),
                normalizarTexto(filtro.getLocal()));
    }

    /**
     * Lista os anuncios que ja foram resolvidos.
     *
     * @return anuncios marcados como resolvidos
     */
    @Transactional(readOnly = true)
    public List<Anuncio> listarResolvidos() {
        return anuncioRepository.buscarPorStatus(StatusAnuncio.RESOLVIDO);
    }

    /**
     * Busca um anuncio pelo identificador.
     *
     * @param id identificador do anuncio
     * @return anuncio encontrado, quando existir
     */
    @Transactional(readOnly = true)
    public Optional<Anuncio> buscarPorId(Long id) {
        return anuncioRepository.buscarPorIdComRelacionamentos(id);
    }

    /**
     * Busca um anuncio e converte seus dados para preenchimento do formulario.
     *
     * @param id identificador do anuncio
     * @return dados do formulario preenchidos, quando o anuncio existir
     */
    @Transactional(readOnly = true)
    public Optional<AnuncioFormDto> buscarFormularioEdicao(Long id) {
        return buscarPorId(id).map(this::criarFormulario);
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
        preencherDadosEditaveis(anuncio, form, categoria);
        imagemStorageService.salvar(form.getImagemArquivo()).ifPresent(anuncio::setImagem);
        anuncio.setData(LocalDate.now());
        anuncio.setStatus(StatusAnuncio.ATIVO);
        anuncio.setUsuario(usuario);

        return anuncioRepository.save(anuncio);
    }

    /**
     * Atualiza os dados editaveis de um anuncio existente.
     *
     * @param id identificador do anuncio
     * @param form dados informados pelo usuario
     * @return anuncio atualizado
     */
    @Transactional
    public Anuncio atualizar(Long id, AnuncioFormDto form) {
        Anuncio anuncio = anuncioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Anuncio nao encontrado."));
        Categoria categoria = categoriaRepository.findById(form.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria nao encontrada."));

        preencherDadosEditaveis(anuncio, form, categoria);
        if (form.isRemoverImagem()) {
            anuncio.setImagem(null);
        }
        imagemStorageService.salvar(form.getImagemArquivo()).ifPresent(anuncio::setImagem);

        return anuncio;
    }

    /**
     * Marca um anuncio como resolvido, retirando-o da listagem de ativos.
     *
     * @param id identificador do anuncio
     */
    @Transactional
    public void resolver(Long id) {
        Anuncio anuncio = anuncioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Anuncio nao encontrado."));

        anuncio.setStatus(StatusAnuncio.RESOLVIDO);
    }

    private AnuncioFormDto criarFormulario(Anuncio anuncio) {
        AnuncioFormDto form = new AnuncioFormDto();
        form.setTitulo(anuncio.getTitulo());
        form.setDescricao(anuncio.getDescricao());
        form.setTipoAnuncio(anuncio.getTipoAnuncio());
        form.setCategoriaId(anuncio.getCategoria().getId());
        form.setLocal(anuncio.getLocal());
        form.setImagemAtual(anuncio.getImagem());
        return form;
    }

    private void preencherDadosEditaveis(Anuncio anuncio, AnuncioFormDto form, Categoria categoria) {
        anuncio.setTitulo(form.getTitulo().trim());
        anuncio.setDescricao(form.getDescricao().trim());
        anuncio.setTipoAnuncio(form.getTipoAnuncio());
        anuncio.setCategoria(categoria);
        anuncio.setLocal(form.getLocal().trim());
    }

    private String normalizarTexto(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }
}
