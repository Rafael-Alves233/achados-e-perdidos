package br.com.achadoseperdidos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.achadoseperdidos.service.AdminService;

/**
 * Controller da area administrativa da aplicacao.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * Exibe os indicadores gerais e os anuncios cadastrados.
     *
     * @param model objeto usado para enviar dados ao template
     * @return nome do template do painel
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("dashboard", adminService.obterDashboard());
        return "admin/dashboard";
    }
}
