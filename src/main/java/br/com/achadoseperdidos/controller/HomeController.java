package br.com.achadoseperdidos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.achadoseperdidos.service.AnuncioService;

/**
 * Controller responsavel pela pagina inicial do sistema.
 */
@Controller
public class HomeController {

    private final AnuncioService anuncioService;

    public HomeController(AnuncioService anuncioService) {
        this.anuncioService = anuncioService;
    }

    /**
     * Exibe a pagina inicial com a lista de anuncios ativos.
     *
     * @param model objeto usado para enviar dados para o template Thymeleaf
     * @return nome do template da pagina inicial
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("anuncios", anuncioService.listarAtivos());
        return "index";
    }
}
