package br.com.achadoseperdidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(properties = "app.upload-dir=target/test-uploads")
@AutoConfigureMockMvc
class AnuncioFluxoIntegrationTest {

    private static final Path TEST_UPLOAD_DIR = Paths.get("target/test-uploads");
    private static final String EMAIL_USUARIO_PADRAO = "secretaria@faculdade.edu";

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbcTemplate;

    private Long documentosId;
    private Long eletronicosId;

    @Autowired
    AnuncioFluxoIntegrationTest(MockMvc mockMvc, JdbcTemplate jdbcTemplate) {
        this.mockMvc = mockMvc;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void prepararDados() {
        jdbcTemplate.update("delete from anuncios");
        jdbcTemplate.update("delete from categorias");
        jdbcTemplate.update("delete from usuarios");

        documentosId = inserirCategoria("Documentos");
        eletronicosId = inserirCategoria("Eletronicos");
        inserirUsuarioPadrao();
    }

    // Teste: cadastro de anuncio e exibicao dos detalhes.
    @Test
    void deveCadastrarEExibirDetalhesDoAnuncio() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Cracha encontrado",
                "Cracha encontrado na biblioteca.",
                "ENCONTRADO",
                documentosId,
                "Biblioteca");

        Map<String, Object> anuncio = buscarAnuncio(anuncioId);

        assertThat(anuncio)
                .containsEntry("titulo", "Cracha encontrado")
                .containsEntry("status", "ATIVO")
                .containsEntry("local", "Biblioteca");
        assertThat(jdbcTemplate.queryForObject("select count(*) from usuarios", Long.class)).isEqualTo(1L);

        mockMvc.perform(get("/anuncios/{id}", anuncioId))
                .andExpect(status().isOk())
                .andExpect(view().name("anuncios/detalhes"))
                .andExpect(content().string(containsString("Cracha encontrado")))
                .andExpect(content().string(containsString("Documentos")))
                .andExpect(content().string(containsString("Secretaria Academica")));
    }

    // Teste: cadastro de anuncio com imagem valida.
    @Test
    void deveCadastrarAnuncioComImagem() throws Exception {
        MockMultipartFile imagem = imagemTeste("cracha.png", "image/png");

        mockMvc.perform(multipart("/anuncios")
                .file(imagem)
                .with(csrf())
                .with(usuarioPadrao())
                .param("titulo", "Cracha com foto")
                .param("descricao", "Cracha encontrado com foto anexada.")
                .param("tipoAnuncio", "ENCONTRADO")
                .param("categoriaId", documentosId.toString())
                .param("local", "Biblioteca"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("mensagem", "Anuncio cadastrado com sucesso."));

        Long anuncioId = jdbcTemplate.queryForObject(
                "select id from anuncios where titulo = ?",
                Long.class,
                "Cracha com foto");
        Map<String, Object> anuncio = buscarAnuncio(anuncioId);
        String imagemSalva = (String) anuncio.get("imagem");

        assertThat(imagemSalva)
                .startsWith("/uploads/")
                .endsWith(".png");

        mockMvc.perform(get("/anuncios/{id}", anuncioId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/uploads/")));

        mockMvc.perform(get(imagemSalva))
                .andExpect(status().isOk())
                .andExpect(content().bytes(new byte[] { 1, 2, 3, 4 }));
    }

    // Teste: rejeicao de upload com arquivo invalido.
    @Test
    void deveRejeitarUploadComTipoDeArquivoInvalido() throws Exception {
        // Garante que a validacao de imagem barra arquivos que nao sao imagens permitidas.
        MockMultipartFile arquivo = imagemTeste("documento.txt", "text/plain");

        mockMvc.perform(multipart("/anuncios")
                .file(arquivo)
                .with(csrf())
                .with(usuarioPadrao())
                .param("titulo", "Arquivo invalido")
                .param("descricao", "Tentativa de upload invalido.")
                .param("tipoAnuncio", "ENCONTRADO")
                .param("categoriaId", documentosId.toString())
                .param("local", "Biblioteca"))
                .andExpect(status().isOk())
                .andExpect(view().name("anuncios/form"))
                .andExpect(content().string(containsString("Envie uma imagem JPG, PNG, GIF ou WEBP.")));

        Long total = jdbcTemplate.queryForObject(
                "select count(*) from anuncios where titulo = ?",
                Long.class,
                "Arquivo invalido");
        assertThat(total).isZero();
    }

    // Teste: filtros da pagina inicial.
    @Test
    void deveFiltrarAnunciosPorTextoTipoCategoriaELocal() throws Exception {
        // Monta anuncios com combinacoes diferentes para validar todos os filtros da home.
        cadastrarAnuncio(
                "Cracha azul",
                "Cracha encontrado no corredor.",
                "ENCONTRADO",
                documentosId,
                "Biblioteca Central");
        cadastrarAnuncio(
                "Fone preto",
                "Fone Bluetooth perdido perto do laboratorio.",
                "PERDIDO",
                eletronicosId,
                "CT-13");
        Long carteiraId = cadastrarAnuncio(
                "Carteira marrom",
                "Carteira encontrada no restaurante.",
                "ENCONTRADO",
                documentosId,
                "RU");
        // Anuncio resolvido nao deve aparecer na listagem principal de ativos.
        jdbcTemplate.update("update anuncios set status = ? where id = ?", "RESOLVIDO", carteiraId);

        mockMvc.perform(get("/")
                .param("termo", "fone")
                .param("tipoAnuncio", "PERDIDO")
                .param("categoriaId", eletronicosId.toString())
                .param("local", "CT"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Fone preto")))
                .andExpect(content().string(not(containsString("Cracha azul"))))
                .andExpect(content().string(not(containsString("Carteira marrom"))));
    }

    // Teste: edicao de anuncio existente.
    @Test
    void deveEditarAnuncioExistente() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Fone perdido",
                "Fone preto perdido no CT.",
                "PERDIDO",
                eletronicosId,
                "CT-13");

        mockMvc.perform(get("/anuncios/{id}/editar", anuncioId)
                .with(usuarioPadrao()))
                .andExpect(status().isOk())
                .andExpect(view().name("anuncios/form"))
                .andExpect(content().string(containsString("Editar anuncio")))
                .andExpect(content().string(containsString("Fone perdido")));

        mockMvc.perform(post("/anuncios/{id}/editar", anuncioId)
                .with(csrf())
                .with(usuarioPadrao())
                .param("titulo", "Fone encontrado")
                .param("descricao", "Fone Bluetooth preto localizado.")
                .param("tipoAnuncio", "ENCONTRADO")
                .param("categoriaId", documentosId.toString())
                .param("local", "Biblioteca"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/anuncios/" + anuncioId))
                .andExpect(flash().attribute("mensagem", "Anuncio atualizado com sucesso."));

        Map<String, Object> anuncio = buscarAnuncio(anuncioId);

        assertThat(anuncio)
                .containsEntry("titulo", "Fone encontrado")
                .containsEntry("descricao", "Fone Bluetooth preto localizado.")
                .containsEntry("tipo_anuncio", "ENCONTRADO")
                .containsEntry("local", "Biblioteca")
                .containsEntry("status", "ATIVO");
        assertThat(anuncio.get("categoria_id")).isEqualTo(documentosId);
    }

    // Teste: substituicao de imagem na edicao.
    @Test
    void deveAtualizarImagemDoAnuncio() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Chave encontrada",
                "Chave encontrada no estacionamento.",
                "ENCONTRADO",
                documentosId,
                "Estacionamento");

        MockMultipartFile imagem = imagemTeste("chave.webp", "image/webp");

        mockMvc.perform(multipart("/anuncios/{id}/editar", anuncioId)
                .file(imagem)
                .with(csrf())
                .with(usuarioPadrao())
                .param("titulo", "Chave encontrada")
                .param("descricao", "Chave encontrada no estacionamento.")
                .param("tipoAnuncio", "ENCONTRADO")
                .param("categoriaId", documentosId.toString())
                .param("local", "Estacionamento"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/anuncios/" + anuncioId))
                .andExpect(flash().attribute("mensagem", "Anuncio atualizado com sucesso."));

        assertThat((String) buscarAnuncio(anuncioId).get("imagem"))
                .startsWith("/uploads/")
                .endsWith(".webp");
    }

    // Teste: remocao do caminho da imagem na edicao.
    @Test
    void deveRemoverImagemDoAnuncioNaEdicao() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Mochila encontrada",
                "Mochila azul encontrada no corredor.",
                "ENCONTRADO",
                documentosId,
                "Corredor");

        jdbcTemplate.update("update anuncios set imagem = ? where id = ?", "/uploads/mochila.png", anuncioId);

        mockMvc.perform(post("/anuncios/{id}/editar", anuncioId)
                .with(csrf())
                .with(usuarioPadrao())
                .param("titulo", "Mochila encontrada")
                .param("descricao", "Mochila azul encontrada no corredor.")
                .param("tipoAnuncio", "ENCONTRADO")
                .param("categoriaId", documentosId.toString())
                .param("local", "Corredor")
                .param("imagemAtual", "/uploads/mochila.png")
                .param("removerImagem", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/anuncios/" + anuncioId))
                .andExpect(flash().attribute("mensagem", "Anuncio atualizado com sucesso."));

        assertThat(buscarAnuncio(anuncioId).get("imagem")).isNull();
    }

    // Teste: remocao do arquivo fisico ao remover imagem.
    @Test
    void deveRemoverArquivoDeUploadAoRemoverImagemNaEdicao() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Mochila com arquivo",
                "Mochila azul encontrada no corredor.",
                "ENCONTRADO",
                documentosId,
                "Corredor");
        // Cria o arquivo fisico para confirmar que remover a imagem tambem limpa o upload.
        Path arquivo = TEST_UPLOAD_DIR.resolve("mochila-remover.png");
        Files.createDirectories(TEST_UPLOAD_DIR);
        Files.write(arquivo, new byte[] { 1, 2, 3 });
        jdbcTemplate.update("update anuncios set imagem = ? where id = ?", "/uploads/mochila-remover.png", anuncioId);

        mockMvc.perform(post("/anuncios/{id}/editar", anuncioId)
                .with(csrf())
                .with(usuarioPadrao())
                .param("titulo", "Mochila com arquivo")
                .param("descricao", "Mochila azul encontrada no corredor.")
                .param("tipoAnuncio", "ENCONTRADO")
                .param("categoriaId", documentosId.toString())
                .param("local", "Corredor")
                .param("imagemAtual", "/uploads/mochila-remover.png")
                .param("removerImagem", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/anuncios/" + anuncioId))
                .andExpect(flash().attribute("mensagem", "Anuncio atualizado com sucesso."));

        assertThat(Files.exists(arquivo)).isFalse();
        assertThat(buscarAnuncio(anuncioId).get("imagem")).isNull();
    }

    // Teste: marcar anuncio como resolvido e listar no historico.
    @Test
    void deveMarcarAnuncioComoResolvidoEListarNoHistorico() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Carteira perdida",
                "Carteira marrom perdida no RU.",
                "PERDIDO",
                documentosId,
                "RU");

        mockMvc.perform(post("/anuncios/{id}/resolver", anuncioId)
                .with(csrf())
                .with(usuarioPadrao()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("mensagem", "Anuncio marcado como resolvido."));

        assertThat(buscarAnuncio(anuncioId)).containsEntry("status", "RESOLVIDO");

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Carteira perdida"))));

        mockMvc.perform(get("/anuncios/resolvidos"))
                .andExpect(status().isOk())
                .andExpect(view().name("anuncios/resolvidos"))
                .andExpect(content().string(containsString("Carteira perdida")))
                .andExpect(content().string(containsString("RESOLVIDO")));
    }

    // Teste: pagina de meus anuncios lista apenas publicacoes do usuario autenticado.
    @Test
    void deveListarApenasAnunciosDoUsuarioAutenticado() throws Exception {
        inserirUsuario("Outro Usuario", "outro@ufes.br", "USUARIO");
        Long anuncioResolvidoId = cadastrarAnuncio(
                "Documento resolvido",
                "Documento que ja foi entregue.",
                "ENCONTRADO",
                documentosId,
                "Secretaria");
        cadastrarAnuncio(
                "Chave da secretaria",
                "Chave encontrada pela secretaria.",
                "ENCONTRADO",
                documentosId,
                "Secretaria");
        cadastrarAnuncioComo(
                "Fone de outro usuario",
                "Fone publicado por outro usuario.",
                "PERDIDO",
                eletronicosId,
                "Laboratorio",
                "outro@ufes.br",
                "USUARIO");
        jdbcTemplate.update("update anuncios set status = ? where id = ?", "RESOLVIDO", anuncioResolvidoId);

        mockMvc.perform(get("/anuncios/meus")
                .with(usuarioPadrao()))
                .andExpect(status().isOk())
                .andExpect(view().name("anuncios/meus"))
                .andExpect(content().string(containsString("Meus anuncios")))
                .andExpect(content().string(containsString("Documento resolvido")))
                .andExpect(content().string(containsString("Chave da secretaria")))
                .andExpect(content().string(containsString("RESOLVIDO")))
                .andExpect(content().string(not(containsString("Fone de outro usuario"))));
    }

    // Teste: acesso aos meus anuncios exige login.
    @Test
    void deveRedirecionarParaLoginAoAcessarMeusAnunciosSemAutenticacao() throws Exception {
        mockMvc.perform(get("/anuncios/meus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    // Teste: perfil mostra dados da conta e resumo dos anuncios do usuario autenticado.
    @Test
    void deveExibirPerfilDoUsuarioAutenticadoComResumoDeAnuncios() throws Exception {
        inserirUsuario("Outro Usuario", "outro@ufes.br", "USUARIO");
        Long anuncioResolvidoId = cadastrarAnuncio(
                "Documento do perfil",
                "Documento usado no resumo do perfil.",
                "ENCONTRADO",
                documentosId,
                "Secretaria");
        cadastrarAnuncio(
                "Chave do perfil",
                "Chave usada no resumo do perfil.",
                "ENCONTRADO",
                documentosId,
                "Secretaria");
        cadastrarAnuncioComo(
                "Objeto de outro perfil",
                "Objeto de outro usuario.",
                "PERDIDO",
                eletronicosId,
                "Laboratorio",
                "outro@ufes.br",
                "USUARIO");
        jdbcTemplate.update("update anuncios set status = ? where id = ?", "RESOLVIDO", anuncioResolvidoId);

        mockMvc.perform(get("/perfil")
                .with(usuarioPadrao()))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/perfil"))
                .andExpect(model().attribute("totalAnuncios", 2L))
                .andExpect(model().attribute("totalAtivos", 1L))
                .andExpect(model().attribute("totalResolvidos", 1L))
                .andExpect(content().string(containsString("Meu perfil")))
                .andExpect(content().string(containsString("Secretaria Academica")))
                .andExpect(content().string(containsString(EMAIL_USUARIO_PADRAO)))
                .andExpect(content().string(containsString("ADMIN")));
    }

    // Teste: painel administrativo com indicadores e listagem de anuncios.
    @Test
    void deveExibirPainelAdministrativoComIndicadoresEAnuncios() throws Exception {
        inserirUsuario("Aluno Demo", "aluno@ufes.br", "USUARIO");
        Long anuncioPerdidoId = cadastrarAnuncio(
                "Notebook perdido",
                "Notebook perdido no laboratorio.",
                "PERDIDO",
                eletronicosId,
                "Laboratorio");
        cadastrarAnuncio(
                "Carteira encontrada",
                "Carteira encontrada na biblioteca.",
                "ENCONTRADO",
                documentosId,
                "Biblioteca");

        mockMvc.perform(post("/anuncios/{id}/resolver", anuncioPerdidoId)
                .with(csrf())
                .with(usuarioPadrao()))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/admin").with(usuarioPadrao()))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("dashboard"))
                .andExpect(content().string(containsString("Notebook perdido")))
                .andExpect(content().string(containsString("Carteira encontrada")))
                .andExpect(content().string(containsString("estado-resolvido")))
                .andExpect(content().string(containsString("estado-encontrado")))
                .andExpect(content().string(containsString("data-testid=\"total-usuarios\">2")))
                .andExpect(content().string(containsString("data-testid=\"total-anuncios\">2")))
                .andExpect(content().string(containsString("data-testid=\"anuncios-ativos\">1")))
                .andExpect(content().string(containsString("data-testid=\"anuncios-resolvidos\">1")))
                .andExpect(content().string(containsString("data-testid=\"anuncios-perdidos\">1")))
                .andExpect(content().string(containsString("data-testid=\"anuncios-encontrados\">1")));
    }

    // Teste: bloqueio do painel para usuario que nao e administrador.
    @Test
    void deveBloquearPainelAdministrativoParaUsuarioComum() throws Exception {
        inserirUsuario("Aluno Demo", "aluno@ufes.br", "USUARIO");

        mockMvc.perform(get("/admin")
                .with(user("aluno@ufes.br").roles("USUARIO")))
                .andExpect(status().isForbidden());
    }

    // Teste: acesso ao perfil exige login.
    @Test
    void deveRedirecionarParaLoginAoAcessarPerfilSemAutenticacao() throws Exception {
        mockMvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    // Teste: atualizacao do nome no perfil.
    @Test
    void deveAtualizarNomeDoPerfil() throws Exception {
        mockMvc.perform(post("/perfil")
                .with(csrf())
                .with(usuarioPadrao())
                .param("nome", "Secretaria Atualizada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attribute("mensagem", "Perfil atualizado com sucesso."));

        String nomeAtualizado = jdbcTemplate.queryForObject(
                "select nome from usuarios where email = ?",
                String.class,
                EMAIL_USUARIO_PADRAO);
        assertThat(nomeAtualizado).isEqualTo("Secretaria Atualizada");

        mockMvc.perform(get("/perfil")
                .with(usuarioPadrao()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Secretaria Atualizada")));
    }

    // Teste: validacao do nome obrigatorio no perfil.
    @Test
    void deveRejeitarAtualizacaoDePerfilComNomeVazio() throws Exception {
        mockMvc.perform(post("/perfil")
                .with(csrf())
                .with(usuarioPadrao())
                .param("nome", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("usuarios/perfil"))
                .andExpect(content().string(containsString("Informe o nome.")));

        String nomeSalvo = jdbcTemplate.queryForObject(
                "select nome from usuarios where email = ?",
                String.class,
                EMAIL_USUARIO_PADRAO);
        assertThat(nomeSalvo).isEqualTo("Secretaria Academica");
    }

    // Teste: exclusao de anuncio existente.
    @Test
    void deveExcluirAnuncioExistente() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Guarda-chuva encontrado",
                "Guarda-chuva preto encontrado na entrada.",
                "ENCONTRADO",
                documentosId,
                "Entrada");

        mockMvc.perform(post("/anuncios/{id}/excluir", anuncioId)
                .with(csrf())
                .with(usuarioPadrao()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("mensagem", "Anuncio excluido com sucesso."));

        Long total = jdbcTemplate.queryForObject(
                "select count(*) from anuncios where id = ?",
                Long.class,
                anuncioId);
        assertThat(total).isZero();

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Guarda-chuva encontrado"))));
    }

    // Teste: erro ao excluir anuncio inexistente.
    @Test
    void deveInformarErroAoExcluirAnuncioInexistente() throws Exception {
        mockMvc.perform(post("/anuncios/{id}/excluir", 9999L)
                .with(csrf())
                .with(usuarioPadrao()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("mensagemErro", "Anuncio nao encontrado."));
    }

    // Teste: bloqueio de exclusao sem permissao.
    @Test
    void deveBloquearExclusaoPorUsuarioSemPermissao() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Notebook perdido",
                "Notebook prata perdido no laboratorio.",
                "PERDIDO",
                eletronicosId,
                "Laboratorio");
        inserirUsuario("Outro Usuario", "outro@ufes.br", "USUARIO");

        mockMvc.perform(post("/anuncios/{id}/excluir", anuncioId)
                .with(csrf())
                .with(user("outro@ufes.br").roles("USUARIO")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("mensagemErro", "Voce nao tem permissao para gerenciar este anuncio."));

        Long total = jdbcTemplate.queryForObject(
                "select count(*) from anuncios where id = ?",
                Long.class,
                anuncioId);
        assertThat(total).isEqualTo(1L);
    }

    // Teste: bloqueio de edicao sem permissao.
    @Test
    void deveBloquearEdicaoPorUsuarioSemPermissao() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Tablet perdido",
                "Tablet perdido no laboratorio.",
                "PERDIDO",
                eletronicosId,
                "Laboratorio");
        // Usuario comum diferente do dono nao pode gerenciar o anuncio.
        inserirUsuario("Outro Usuario", "outro@ufes.br", "USUARIO");

        mockMvc.perform(post("/anuncios/{id}/editar", anuncioId)
                .with(csrf())
                .with(user("outro@ufes.br").roles("USUARIO"))
                .param("titulo", "Tablet alterado")
                .param("descricao", "Tentativa de alteracao.")
                .param("tipoAnuncio", "ENCONTRADO")
                .param("categoriaId", documentosId.toString())
                .param("local", "Biblioteca"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("mensagemErro", "Voce nao tem permissao para gerenciar este anuncio."));

        Map<String, Object> anuncio = buscarAnuncio(anuncioId);
        assertThat(anuncio)
                .containsEntry("titulo", "Tablet perdido")
                .containsEntry("tipo_anuncio", "PERDIDO")
                .containsEntry("local", "Laboratorio");
    }

    // Teste: bloqueio de resolucao sem permissao.
    @Test
    void deveBloquearResolucaoPorUsuarioSemPermissao() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Calculadora perdida",
                "Calculadora cientifica perdida na sala.",
                "PERDIDO",
                eletronicosId,
                "Sala 12");
        // A mesma regra de permissao tambem protege a acao de resolver.
        inserirUsuario("Outro Usuario", "outro@ufes.br", "USUARIO");

        mockMvc.perform(post("/anuncios/{id}/resolver", anuncioId)
                .with(csrf())
                .with(user("outro@ufes.br").roles("USUARIO")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("mensagemErro", "Voce nao tem permissao para gerenciar este anuncio."));

        assertThat(buscarAnuncio(anuncioId)).containsEntry("status", "ATIVO");
    }

    // Teste: remocao do arquivo de upload ao excluir anuncio.
    @Test
    void deveRemoverArquivoDeUploadAoExcluirAnuncio() throws Exception {
        MockMultipartFile imagem = imagemTeste("relogio.png", "image/png");

        mockMvc.perform(multipart("/anuncios")
                .file(imagem)
                .with(csrf())
                .with(usuarioPadrao())
                .param("titulo", "Relogio encontrado")
                .param("descricao", "Relogio encontrado no laboratorio.")
                .param("tipoAnuncio", "ENCONTRADO")
                .param("categoriaId", documentosId.toString())
                .param("local", "Laboratorio"))
                .andExpect(status().is3xxRedirection());

        Long anuncioId = jdbcTemplate.queryForObject(
                "select id from anuncios where titulo = ?",
                Long.class,
                "Relogio encontrado");
        String imagemSalva = (String) buscarAnuncio(anuncioId).get("imagem");
        Path arquivo = TEST_UPLOAD_DIR.resolve(imagemSalva.substring("/uploads/".length()));

        assertThat(Files.exists(arquivo)).isTrue();

        mockMvc.perform(post("/anuncios/{id}/excluir", anuncioId)
                .with(csrf())
                .with(usuarioPadrao()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(Files.exists(arquivo)).isFalse();
    }

    // Teste: cadastro de usuario, senha criptografada e login.
    @Test
    void deveCadastrarUsuarioComSenhaCriptografadaEPermitirLogin() throws Exception {
        mockMvc.perform(post("/cadastro")
                .with(csrf())
                .param("nome", "Aluno Teste")
                .param("email", "aluno@ufes.br")
                .param("senha", "123456")
                .param("confirmacaoSenha", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attribute("mensagem", "Usuario cadastrado com sucesso. Faca login para continuar."));

        String senhaSalva = jdbcTemplate.queryForObject(
                "select senha from usuarios where email = ?",
                String.class,
                "aluno@ufes.br");

        assertThat(senhaSalva)
                .isNotEqualTo("123456")
                .startsWith("$2");

        mockMvc.perform(formLogin("/login")
                .user("username", "aluno@ufes.br")
                .password("password", "123456"))
                .andExpect(authenticated().withUsername("aluno@ufes.br"));
    }

    // Teste: rejeicao de cadastro com e-mail duplicado.
    @Test
    void deveRejeitarCadastroDeUsuarioComEmailDuplicado() throws Exception {
        // O cadastro normaliza e-mail, entao caixa alta/minuscula nao cria duplicidade.
        inserirUsuario("Aluno Existente", "aluno@ufes.br", "USUARIO");

        mockMvc.perform(post("/cadastro")
                .with(csrf())
                .param("nome", "Outro Aluno")
                .param("email", "ALUNO@UFES.BR")
                .param("senha", "123456")
                .param("confirmacaoSenha", "123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/cadastro"))
                .andExpect(content().string(containsString("E-mail ja cadastrado.")));

        Long total = jdbcTemplate.queryForObject(
                "select count(*) from usuarios where email = ?",
                Long.class,
                "aluno@ufes.br");
        assertThat(total).isEqualTo(1L);
    }

    // Teste: rejeicao de cadastro com senhas diferentes.
    @Test
    void deveRejeitarCadastroDeUsuarioComSenhasDiferentes() throws Exception {
        // Senhas divergentes devolvem erro no formulario e nao persistem usuario.
        mockMvc.perform(post("/cadastro")
                .with(csrf())
                .param("nome", "Aluno Teste")
                .param("email", "aluno@ufes.br")
                .param("senha", "123456")
                .param("confirmacaoSenha", "654321"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/cadastro"))
                .andExpect(content().string(containsString("As senhas informadas nao conferem.")));

        Long total = jdbcTemplate.queryForObject(
                "select count(*) from usuarios where email = ?",
                Long.class,
                "aluno@ufes.br");
        assertThat(total).isZero();
    }

    // Teste: redirecionamento para login sem autenticacao.
    @Test
    void deveRedirecionarParaLoginAoTentarCadastrarAnuncioSemAutenticacao() throws Exception {
        mockMvc.perform(post("/anuncios")
                .with(csrf())
                .param("titulo", "Objeto sem login")
                .param("descricao", "Tentativa sem usuario autenticado.")
                .param("tipoAnuncio", "PERDIDO")
                .param("categoriaId", documentosId.toString())
                .param("local", "Biblioteca"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    private Long cadastrarAnuncio(
            String titulo,
            String descricao,
            String tipoAnuncio,
            Long categoriaId,
            String local) throws Exception {
        return cadastrarAnuncioComo(
                titulo,
                descricao,
                tipoAnuncio,
                categoriaId,
                local,
                EMAIL_USUARIO_PADRAO,
                "ADMIN");
    }

    private Long cadastrarAnuncioComo(
            String titulo,
            String descricao,
            String tipoAnuncio,
            Long categoriaId,
            String local,
            String email,
            String role) throws Exception {
        mockMvc.perform(post("/anuncios")
                .with(csrf())
                .with(user(email).roles(role))
                .param("titulo", titulo)
                .param("descricao", descricao)
                .param("tipoAnuncio", tipoAnuncio)
                .param("categoriaId", categoriaId.toString())
                .param("local", local))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("mensagem", "Anuncio cadastrado com sucesso."));

        return jdbcTemplate.queryForObject(
                "select id from anuncios where titulo = ?",
                Long.class,
                titulo);
    }

    private Long inserirCategoria(String nome) {
        jdbcTemplate.update("insert into categorias (nome) values (?)", nome);
        return jdbcTemplate.queryForObject(
                "select id from categorias where nome = ?",
                Long.class,
                nome);
    }

    private void inserirUsuarioPadrao() {
        inserirUsuario("Secretaria Academica", EMAIL_USUARIO_PADRAO, "ADMIN");
    }

    private void inserirUsuario(String nome, String email, String tipoUsuario) {
        jdbcTemplate.update(
                "insert into usuarios (nome, email, senha, tipo_usuario) values (?, ?, ?, ?)",
                nome,
                email,
                "$2a$10$vlb6wG7fGp0WAqMK/e6fuO3oaV3LbCQjRYVlK9B3qu1VK0YGvvCia",
                tipoUsuario);
    }

    private Map<String, Object> buscarAnuncio(Long id) {
        return jdbcTemplate.queryForMap("select * from anuncios where id = ?", id);
    }

    private RequestPostProcessor usuarioPadrao() {
        return user(EMAIL_USUARIO_PADRAO).roles("ADMIN");
    }

    private MockMultipartFile imagemTeste(String nomeArquivo, String contentType) {
        return new MockMultipartFile(
                "imagemArquivo",
                nomeArquivo,
                contentType,
                new byte[] { 1, 2, 3, 4 });
    }
}
