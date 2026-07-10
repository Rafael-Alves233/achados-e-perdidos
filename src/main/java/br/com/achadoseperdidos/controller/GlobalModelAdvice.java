package br.com.achadoseperdidos.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;

import br.com.achadoseperdidos.model.TipoUsuario;
import br.com.achadoseperdidos.service.UsuarioService;

/**
 * Adiciona dados comuns usados pelos templates.
 */
@ControllerAdvice
public class GlobalModelAdvice {

    private final UsuarioService usuarioService;

    public GlobalModelAdvice(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @ModelAttribute
    public void adicionarUsuarioLogado(Model model) {
        usuarioService.buscarUsuarioAutenticado().ifPresentOrElse(
                usuario -> {
                    model.addAttribute("usuarioLogado", usuario);
                    model.addAttribute("autenticado", true);
                    model.addAttribute("administrador", usuario.getTipoUsuario() == TipoUsuario.ADMIN);
                },
                () -> {
                    model.addAttribute("usuarioLogado", null);
                    model.addAttribute("autenticado", false);
                    model.addAttribute("administrador", false);
                });
    }
}
