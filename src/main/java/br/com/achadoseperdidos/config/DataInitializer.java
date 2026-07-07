package br.com.achadoseperdidos.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.achadoseperdidos.model.Anuncio;
import br.com.achadoseperdidos.model.Categoria;
import br.com.achadoseperdidos.model.StatusAnuncio;
import br.com.achadoseperdidos.model.TipoAnuncio;
import br.com.achadoseperdidos.model.TipoUsuario;
import br.com.achadoseperdidos.model.Usuario;
import br.com.achadoseperdidos.repository.AnuncioRepository;
import br.com.achadoseperdidos.repository.CategoriaRepository;
import br.com.achadoseperdidos.repository.UsuarioRepository;
import br.com.achadoseperdidos.service.UsuarioService;

/**
 * Configuracao responsavel por carregar dados iniciais para demonstracao em
 * ambiente de desenvolvimento.
 */
@Configuration
public class DataInitializer {

    private static final String EMAIL_USUARIO_DEMO = "aluno@faculdade.edu";
    private static final String SENHA_USUARIO_DEMO = "123456";

    /**
     * Garante categorias e usuarios padrao, alem de criar anuncios de exemplo
     * para cada usuario de demonstracao.
     *
     * @param usuarioService servico de usuarios
     * @param usuarioRepository repositorio de usuarios
     * @param categoriaRepository repositorio de categorias
     * @param anuncioRepository repositorio de anuncios
     * @param passwordEncoder codificador usado para salvar a senha do usuario demo
     * @return rotina executada na inicializacao da aplicacao
     */
    @Bean
    CommandLineRunner carregarDadosIniciais(
            UsuarioService usuarioService,
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            AnuncioRepository anuncioRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            Usuario usuarioAdmin = usuarioService.obterOuCriarUsuarioPadrao();
            Usuario usuarioDemo = obterOuCriarUsuarioDemo(usuarioRepository, passwordEncoder);

            Categoria documentos = categoriaPadrao("Documentos", categoriaRepository);
            Categoria eletronicos = categoriaPadrao("Eletronicos", categoriaRepository);
            Categoria outros = categoriaPadrao("Outros", categoriaRepository);

            salvarAnuncioSeNaoExistir(
                    anuncioRepository,
                    usuarioAdmin,
                    "Cracha encontrado",
                    "Cracha de estudante encontrado proximo a biblioteca central.",
                    TipoAnuncio.ENCONTRADO,
                    documentos,
                    "Biblioteca central",
                    LocalDate.now().minusDays(1),
                    StatusAnuncio.ATIVO,
                    "/images/demo-cracha.png");

            salvarAnuncioSeNaoExistir(
                    anuncioRepository,
                    usuarioAdmin,
                    "Fone de ouvido perdido",
                    "Fone Bluetooth preto perdido no CT-13.",
                    TipoAnuncio.PERDIDO,
                    eletronicos,
                    "CT-13",
                    LocalDate.now(),
                    StatusAnuncio.ATIVO,
                    "/images/demo-fone.png");

            salvarAnuncioSeNaoExistir(
                    anuncioRepository,
                    usuarioDemo,
                    "Mochila azul perdida",
                    "Mochila azul com livros e caderno de calculo perdida no CT-9.",
                    TipoAnuncio.PERDIDO,
                    outros,
                    "CT-9",
                    LocalDate.now().minusDays(3),
                    StatusAnuncio.ATIVO,
                    null);

            salvarAnuncioSeNaoExistir(
                    anuncioRepository,
                    usuarioDemo,
                    "Caderno encontrado",
                    "Caderno azul encontrado depois da aula de TBO.",
                    TipoAnuncio.ENCONTRADO,
                    outros,
                    "LabGrad-1",
                    LocalDate.now().minusDays(2),
                    StatusAnuncio.ATIVO,
                    null);

            salvarAnuncioSeNaoExistir(
                    anuncioRepository,
                    usuarioDemo,
                    "Documento devolvido",
                    "Documento encontrado na secretaria e ja entregue ao dono.",
                    TipoAnuncio.ENCONTRADO,
                    documentos,
                    "Secretaria academica",
                    LocalDate.now().minusDays(5),
                    StatusAnuncio.RESOLVIDO,
                    null);

        };
    }

    private Usuario obterOuCriarUsuarioDemo(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return usuarioRepository.findByEmail(EMAIL_USUARIO_DEMO)
                .orElseGet(() -> {
                    Usuario usuario = new Usuario();
                    usuario.setNome("Aluno Demo");
                    usuario.setEmail(EMAIL_USUARIO_DEMO);
                    usuario.setSenha(passwordEncoder.encode(SENHA_USUARIO_DEMO));
                    usuario.setTipoUsuario(TipoUsuario.USUARIO);
                    return usuarioRepository.save(usuario);
                });
    }

    private void salvarAnuncioSeNaoExistir(
            AnuncioRepository anuncioRepository,
            Usuario usuario,
            String titulo,
            String descricao,
            TipoAnuncio tipoAnuncio,
            Categoria categoria,
            String local,
            LocalDate data,
            StatusAnuncio status,
            String imagem) {
        if (anuncioRepository.existsByTituloAndUsuario(titulo, usuario)) {
            return;
        }

        Anuncio anuncio = new Anuncio();
        anuncio.setTitulo(titulo);
        anuncio.setDescricao(descricao);
        anuncio.setTipoAnuncio(tipoAnuncio);
        anuncio.setCategoria(categoria);
        anuncio.setLocal(local);
        anuncio.setData(data);
        anuncio.setStatus(status);
        anuncio.setImagem(imagem);
        anuncio.setUsuario(usuario);
        anuncioRepository.save(anuncio);
    }

    private Categoria categoriaPadrao(String nome, CategoriaRepository categoriaRepository) {
        return categoriaRepository.findByNome(nome)
                .orElseGet(() -> novaCategoria(nome, categoriaRepository));
    }

    private Categoria novaCategoria(String nome, CategoriaRepository categoriaRepository) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
    }
}
