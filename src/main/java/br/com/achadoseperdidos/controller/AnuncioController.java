package br.com.achadoseperdidos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.achadoseperdidos.dto.AnuncioFormDto;
import br.com.achadoseperdidos.model.Anuncio;
import br.com.achadoseperdidos.model.TipoAnuncio;
import br.com.achadoseperdidos.service.AnuncioService;
import jakarta.validation.Valid;

/**
 * Controller responsavel pelo fluxo de cadastro de anuncios.
 */
@Controller
@RequestMapping("/anuncios")
public class AnuncioController {

    private final AnuncioService anuncioService;

    /**
     * Cria o controller com o servico que executa as operacoes de anuncios.
     *
     * @param anuncioService servico responsavel pelas regras de anuncios
     */
    public AnuncioController(AnuncioService anuncioService) {
        this.anuncioService = anuncioService;
    }

    /**
     * Exibe o formulario de cadastro de anuncio.
     *
     * @param model objeto usado para enviar dados para o template Thymeleaf
     * @return nome do template do formulario
     */
    @GetMapping("/novo")
    public String novo(Model model) {
        if (!model.containsAttribute("anuncioFormDto")) {
            model.addAttribute("anuncioFormDto", new AnuncioFormDto());
        }
        carregarDadosFormularioCadastro(model);
        return "anuncios/form";
    }

    /**
     * Exibe os anuncios que ja foram marcados como resolvidos.
     *
     * @param model objeto usado para enviar dados para o template Thymeleaf
     * @return nome do template de anuncios resolvidos
     */
    @GetMapping("/resolvidos")
    public String resolvidos(Model model) {
        model.addAttribute("anuncios", anuncioService.listarResolvidos());
        return "anuncios/resolvidos";
    }

    /**
     * Exibe os anuncios publicados pelo usuario autenticado.
     *
     * @param model objeto usado para enviar dados para o template Thymeleaf
     * @return nome do template de anuncios do usuario
     */
    @GetMapping("/meus")
    public String meus(Model model) {
        model.addAttribute("anuncios", anuncioService.listarDoUsuarioAutenticado());
        return "anuncios/meus";
    }

    /**
     * Exibe o formulario de edicao de um anuncio.
     *
     * @param id identificador do anuncio
     * @param model objeto usado para enviar dados para o template Thymeleaf
     * @param redirectAttributes atributos enviados apos redirecionamento
     * @return nome do template do formulario ou redirecionamento para a pagina inicial
     */
    @GetMapping("/{id}/editar")
    public String editar(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (model.containsAttribute("anuncioFormDto")) {
            carregarDadosFormularioEdicao(model, id);
            return "anuncios/form";
        }

        try {
            return anuncioService.buscarFormularioEdicao(id)
                    .map(form -> {
                        model.addAttribute("anuncioFormDto", form);
                        carregarDadosFormularioEdicao(model, id);
                        return "anuncios/form";
                    })
                    .orElseGet(() -> {
                        redirectAttributes.addFlashAttribute("mensagemErro", "Anuncio nao encontrado.");
                        return "redirect:/";
                    });
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("mensagemErro", exception.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Exibe os detalhes de um anuncio especifico.
     *
     * @param id identificador do anuncio
     * @param model objeto usado para enviar dados para o template Thymeleaf
     * @param redirectAttributes atributos enviados apos redirecionamento
     * @return nome do template de detalhes ou redirecionamento para a pagina inicial
     */
    @GetMapping("/{id}")
    public String detalhes(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {
        return anuncioService.buscarPorId(id)
                .map(anuncio -> exibirDetalhes(anuncio, model))
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("mensagemErro", "Anuncio nao encontrado.");
                    return "redirect:/";
                });
    }

    /**
     * Salva um novo anuncio quando os dados do formulario sao validos.
     *
     * @param anuncioFormDto dados preenchidos no formulario
     * @param bindingResult resultado da validacao do formulario
     * @param model objeto usado para reenviar dados para o template
     * @param redirectAttributes atributos enviados apos redirecionamento
     * @return redirecionamento para a pagina inicial ou formulario com erros
     */
    @PostMapping
    public String salvar(
            @Valid @ModelAttribute AnuncioFormDto anuncioFormDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            carregarDadosFormularioCadastro(model);
            return "anuncios/form";
        }

        try {
            anuncioService.salvar(anuncioFormDto);
        } catch (IllegalArgumentException exception) {
            rejeitarErroDeFormulario(bindingResult, exception);
            carregarDadosFormularioCadastro(model);
            return "anuncios/form";
        }

        redirectAttributes.addFlashAttribute("mensagem", "Anuncio cadastrado com sucesso.");
        return "redirect:/";
    }

    /**
     * Atualiza um anuncio existente quando os dados do formulario sao validos.
     *
     * @param id identificador do anuncio
     * @param anuncioFormDto dados preenchidos no formulario
     * @param bindingResult resultado da validacao do formulario
     * @param model objeto usado para reenviar dados para o template
     * @param redirectAttributes atributos enviados apos redirecionamento
     * @return redirecionamento para os detalhes ou formulario com erros
     */
    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute AnuncioFormDto anuncioFormDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            carregarDadosFormularioEdicao(model, id);
            return "anuncios/form";
        }

        try {
            anuncioService.atualizar(id, anuncioFormDto);
        } catch (IllegalArgumentException exception) {
            if (erroDeFormulario(exception)) {
                rejeitarErroDeFormulario(bindingResult, exception);
                carregarDadosFormularioEdicao(model, id);
                return "anuncios/form";
            }

            redirectAttributes.addFlashAttribute("mensagemErro", exception.getMessage());
            return "redirect:/";
        }

        redirectAttributes.addFlashAttribute("mensagem", "Anuncio atualizado com sucesso.");
        return "redirect:/anuncios/" + id;
    }

    /**
     * Marca um anuncio como resolvido e retorna para a pagina inicial.
     *
     * @param id identificador do anuncio
     * @param redirectAttributes atributos enviados apos redirecionamento
     * @return redirecionamento para a pagina inicial
     */
    @PostMapping("/{id}/resolver")
    public String resolver(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            anuncioService.resolver(id);
            redirectAttributes.addFlashAttribute("mensagem", "Anuncio marcado como resolvido.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("mensagemErro", exception.getMessage());
        }

        return "redirect:/";
    }

    /**
     * Exclui um anuncio e retorna para a pagina inicial.
     *
     * @param id identificador do anuncio
     * @param redirectAttributes atributos enviados apos redirecionamento
     * @return redirecionamento para a pagina inicial
     */
    @PostMapping("/{id}/excluir")
    public String excluir(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            anuncioService.excluir(id);
            redirectAttributes.addFlashAttribute("mensagem", "Anuncio excluido com sucesso.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("mensagemErro", exception.getMessage());
        }

        return "redirect:/";
    }

    private void carregarDadosFormularioCadastro(Model model) {
        carregarOpcoesFormulario(model);
        model.addAttribute("modoEdicao", false);
        model.addAttribute("tituloPagina", "Cadastrar anuncio");
        model.addAttribute("subtituloPagina", "Registre um objeto perdido ou encontrado no campus.");
        model.addAttribute("rotuloSecao", "Novo registro");
        model.addAttribute("rotuloBotao", "Salvar anuncio");
    }

    private void carregarDadosFormularioEdicao(Model model, Long id) {
        carregarOpcoesFormulario(model);
        model.addAttribute("modoEdicao", true);
        model.addAttribute("anuncioId", id);
        model.addAttribute("tituloPagina", "Editar anuncio");
        model.addAttribute("subtituloPagina", "Atualize as informacoes do anuncio selecionado.");
        model.addAttribute("rotuloSecao", "Edicao");
        model.addAttribute("rotuloBotao", "Salvar alteracoes");
    }

    private void carregarOpcoesFormulario(Model model) {
        model.addAttribute("tiposAnuncio", TipoAnuncio.values());
        model.addAttribute("categorias", anuncioService.listarCategorias());
    }

    private boolean erroDeFormulario(IllegalArgumentException exception) {
        return "Categoria nao encontrada.".equals(exception.getMessage())
                || exception.getMessage().contains("imagem")
                || exception.getMessage().contains("arquivo")
                || exception.getMessage().contains("JPG");
    }

    private void rejeitarErroDeFormulario(BindingResult bindingResult, IllegalArgumentException exception) {
        if ("Categoria nao encontrada.".equals(exception.getMessage())) {
            bindingResult.rejectValue("categoriaId", "categoria.invalida", exception.getMessage());
            return;
        }

        bindingResult.rejectValue("imagemArquivo", "imagem.invalida", exception.getMessage());
    }

    private String exibirDetalhes(Anuncio anuncio, Model model) {
        model.addAttribute("anuncio", anuncio);
        model.addAttribute("podeGerenciar", anuncioService.podeGerenciar(anuncio));
        return "anuncios/detalhes";
    }
}
