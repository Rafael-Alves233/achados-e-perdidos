package br.com.achadoseperdidos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import br.com.achadoseperdidos.dto.AnuncioFiltroDto;
import br.com.achadoseperdidos.model.TipoAnuncio;
import br.com.achadoseperdidos.service.AnuncioService;

/**
 * Controller responsavel pela pagina inicial do sistema.
 */
@Controller
public class HomeController {

    private final AnuncioService anuncioService;

    /**
     * Cria o controller com o servico usado na listagem inicial.
     *
     * @param anuncioService servico responsavel pelos anuncios
     */
    public HomeController(AnuncioService anuncioService) {
        this.anuncioService = anuncioService;
    }

    /**
     * Exibe a pagina inicial com a lista de anuncios ativos.
     *
     * @param filtro filtros aplicados na listagem
     * @param model objeto usado para enviar dados para o template Thymeleaf
     * @return nome do template da pagina inicial
     */
    @GetMapping("/")
    public String index(@ModelAttribute("filtro") AnuncioFiltroDto filtro, Model model) {
        model.addAttribute("anuncios", anuncioService.listarAtivos(filtro));
        model.addAttribute("tiposAnuncio", TipoAnuncio.values());
        model.addAttribute("categorias", anuncioService.listarCategorias());
        return "index";
    }
}
