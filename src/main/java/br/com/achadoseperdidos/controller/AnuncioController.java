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
        carregarDadosFormulario(model);
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
            carregarDadosFormulario(model);
            return "anuncios/form";
        }

        try {
            anuncioService.salvar(anuncioFormDto);
        } catch (IllegalArgumentException exception) {
            bindingResult.rejectValue("categoriaId", "categoria.invalida", exception.getMessage());
            carregarDadosFormulario(model);
            return "anuncios/form";
        }

        redirectAttributes.addFlashAttribute("mensagem", "Anuncio cadastrado com sucesso.");
        return "redirect:/";
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

    private void carregarDadosFormulario(Model model) {
        model.addAttribute("tiposAnuncio", TipoAnuncio.values());
        model.addAttribute("categorias", anuncioService.listarCategorias());
    }

    private String exibirDetalhes(Anuncio anuncio, Model model) {
        model.addAttribute("anuncio", anuncio);
        return "anuncios/detalhes";
    }
}
