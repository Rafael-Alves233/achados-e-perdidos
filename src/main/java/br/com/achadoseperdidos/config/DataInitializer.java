package br.com.achadoseperdidos.config;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.achadoseperdidos.model.Anuncio;
import br.com.achadoseperdidos.model.Categoria;
import br.com.achadoseperdidos.model.TipoAnuncio;
import br.com.achadoseperdidos.model.TipoUsuario;
import br.com.achadoseperdidos.model.Usuario;
import br.com.achadoseperdidos.repository.AnuncioRepository;
import br.com.achadoseperdidos.repository.CategoriaRepository;
import br.com.achadoseperdidos.repository.UsuarioRepository;

/**
 * Configuracao responsavel por carregar dados iniciais para demonstracao em
 * ambiente de desenvolvimento.
 */
@Configuration
public class DataInitializer {

    /**
     * Cria categorias, usuario e anuncios de exemplo quando o banco esta vazio.
     *
     * @param usuarioRepository repositorio de usuarios
     * @param categoriaRepository repositorio de categorias
     * @param anuncioRepository repositorio de anuncios
     * @return rotina executada na inicializacao da aplicacao
     */
    @Bean
    CommandLineRunner carregarDadosIniciais(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            AnuncioRepository anuncioRepository) {
        return args -> {
            if (anuncioRepository.count() > 0) {
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setNome("Secretaria Academica");
            usuario.setEmail("secretaria@faculdade.edu");
            usuario.setSenha("123456");
            usuario.setTipoUsuario(TipoUsuario.ADMIN);
            usuarioRepository.save(usuario);

            Categoria documentos = novaCategoria("Documentos", categoriaRepository);
            Categoria eletronicos = novaCategoria("Eletronicos", categoriaRepository);

            Anuncio cracha = new Anuncio();
            cracha.setTitulo("Cracha encontrado");
            cracha.setDescricao("Cracha de estudante encontrado proximo a biblioteca central.");
            cracha.setTipoAnuncio(TipoAnuncio.ENCONTRADO);
            cracha.setCategoria(documentos);
            cracha.setLocal("Biblioteca central");
            cracha.setData(LocalDate.now().minusDays(1));
            cracha.setUsuario(usuario);
            anuncioRepository.save(cracha);

            Anuncio fone = new Anuncio();
            fone.setTitulo("Fone de ouvido perdido");
            fone.setDescricao("Fone Bluetooth preto perdido no CT-13.");
            fone.setTipoAnuncio(TipoAnuncio.PERDIDO);
            fone.setCategoria(eletronicos);
            fone.setLocal("CT-13");
            fone.setData(LocalDate.now());
            fone.setUsuario(usuario);
            anuncioRepository.save(fone);
        };
    }

    private Categoria novaCategoria(String nome, CategoriaRepository categoriaRepository) {
        Categoria categoria = new Categoria();
        categoria.setNome(nome);
        return categoriaRepository.save(categoria);
    }
}
