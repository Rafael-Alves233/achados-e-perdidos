package br.com.achadoseperdidos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.achadoseperdidos.dto.UsuarioCadastroDto;
import br.com.achadoseperdidos.service.UsuarioService;
import jakarta.validation.Valid;

/**
 * Controller responsavel pelos fluxos de cadastro e login de usuarios.
 */
@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    /**
     * Cria o controller com o servico usado no cadastro de usuarios.
     *
     * @param usuarioService servico responsavel pelas operacoes de usuario
     */
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * Exibe a pagina de login.
     *
     * @return nome do template de login
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /**
     * Exibe o formulario de cadastro de usuario.
     *
     * @param model objeto usado para enviar dados para o template
     * @return nome do template de cadastro
     */
    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        if (!model.containsAttribute("usuarioCadastroDto")) {
            model.addAttribute("usuarioCadastroDto", new UsuarioCadastroDto());
        }
        return "auth/cadastro";
    }

    /**
     * Cadastra um novo usuario comum.
     *
     * @param usuarioCadastroDto dados do formulario
     * @param bindingResult resultado da validacao
     * @param redirectAttributes atributos enviados apos redirecionamento
     * @return redirecionamento para login ou formulario com erros
     */
    @PostMapping("/cadastro")
    public String cadastrar(
            @Valid @ModelAttribute UsuarioCadastroDto usuarioCadastroDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/cadastro";
        }

        try {
            usuarioService.cadastrar(usuarioCadastroDto);
        } catch (IllegalArgumentException exception) {
            if ("E-mail ja cadastrado.".equals(exception.getMessage())) {
                bindingResult.rejectValue("email", "email.duplicado", exception.getMessage());
            } else {
                bindingResult.rejectValue("confirmacaoSenha", "senha.invalida", exception.getMessage());
            }
            return "auth/cadastro";
        }

        redirectAttributes.addFlashAttribute("mensagem", "Usuario cadastrado com sucesso. Faca login para continuar.");
        return "redirect:/login";
    }
}
