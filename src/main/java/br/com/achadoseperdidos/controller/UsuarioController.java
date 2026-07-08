package br.com.achadoseperdidos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.achadoseperdidos.dto.UsuarioPerfilDto;
import br.com.achadoseperdidos.model.StatusAnuncio;
import br.com.achadoseperdidos.model.Usuario;
import br.com.achadoseperdidos.service.AnuncioService;
import br.com.achadoseperdidos.service.UsuarioService;
import jakarta.validation.Valid;

/**
 * Controller responsavel pela pagina de perfil do usuario autenticado.
 */
@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final AnuncioService anuncioService;

    public UsuarioController(UsuarioService usuarioService, AnuncioService anuncioService) {
        this.usuarioService = usuarioService;
        this.anuncioService = anuncioService;
    }

    /**
     * Exibe o perfil do usuario autenticado.
     *
     * @param model objeto usado para enviar dados ao template
     * @return nome do template de perfil
     */
    @GetMapping("/perfil")
    public String perfil(Model model) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();
        if (!model.containsAttribute("usuarioPerfilDto")) {
            model.addAttribute("usuarioPerfilDto", UsuarioPerfilDto.from(usuario));
        }
        adicionarDadosPerfil(model, usuario);
        return "usuarios/perfil";
    }

    /**
     * Atualiza o nome exibido no perfil do usuario autenticado.
     *
     * @param usuarioPerfilDto dados preenchidos no formulario
     * @param bindingResult resultado da validacao
     * @param model objeto usado para reenviar dados ao template
     * @param redirectAttributes atributos enviados apos redirecionamento
     * @return redirecionamento para o perfil ou template com erros
     */
    @PostMapping("/perfil")
    public String atualizarPerfil(
            @Valid @ModelAttribute UsuarioPerfilDto usuarioPerfilDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.obterUsuarioAutenticado();

        if (bindingResult.hasErrors()) {
            adicionarDadosPerfil(model, usuario);
            return "usuarios/perfil";
        }

        usuarioService.atualizarPerfil(usuarioPerfilDto);
        redirectAttributes.addFlashAttribute("mensagem", "Perfil atualizado com sucesso.");
        return "redirect:/perfil";
    }

    private void adicionarDadosPerfil(Model model, Usuario usuario) {
        model.addAttribute("usuarioPerfil", usuario);
        model.addAttribute("totalAnuncios", anuncioService.contarPorUsuario(usuario));
        model.addAttribute("totalAtivos", anuncioService.contarPorUsuarioEStatus(usuario, StatusAnuncio.ATIVO));
        model.addAttribute("totalResolvidos", anuncioService.contarPorUsuarioEStatus(usuario, StatusAnuncio.RESOLVIDO));
    }
}
