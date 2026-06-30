package br.com.achadoseperdidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest(properties = "app.upload-dir=target/test-uploads")
@AutoConfigureMockMvc
class AnuncioFluxoIntegrationTest {

    private static final Path TEST_UPLOAD_DIR = Paths.get("target/test-uploads");

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
    }

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

    @Test
    void deveCadastrarAnuncioComImagem() throws Exception {
        MockMultipartFile imagem = imagemTeste("cracha.png", "image/png");

        mockMvc.perform(multipart("/anuncios")
                .file(imagem)
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

        assertThat((String) anuncio.get("imagem"))
                .startsWith("/uploads/")
                .endsWith(".png");

        mockMvc.perform(get("/anuncios/{id}", anuncioId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/uploads/")));
    }

    @Test
    void deveEditarAnuncioExistente() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Fone perdido",
                "Fone preto perdido no CT.",
                "PERDIDO",
                eletronicosId,
                "CT-13");

        mockMvc.perform(get("/anuncios/{id}/editar", anuncioId))
                .andExpect(status().isOk())
                .andExpect(view().name("anuncios/form"))
                .andExpect(content().string(containsString("Editar anuncio")))
                .andExpect(content().string(containsString("Fone perdido")));

        mockMvc.perform(post("/anuncios/{id}/editar", anuncioId)
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

    @Test
    void deveMarcarAnuncioComoResolvidoEListarNoHistorico() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Carteira perdida",
                "Carteira marrom perdida no RU.",
                "PERDIDO",
                documentosId,
                "RU");

        mockMvc.perform(post("/anuncios/{id}/resolver", anuncioId))
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

    @Test
    void deveExcluirAnuncioExistente() throws Exception {
        Long anuncioId = cadastrarAnuncio(
                "Guarda-chuva encontrado",
                "Guarda-chuva preto encontrado na entrada.",
                "ENCONTRADO",
                documentosId,
                "Entrada");

        mockMvc.perform(post("/anuncios/{id}/excluir", anuncioId))
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

    @Test
    void deveInformarErroAoExcluirAnuncioInexistente() throws Exception {
        mockMvc.perform(post("/anuncios/{id}/excluir", 9999L))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute("mensagemErro", "Anuncio nao encontrado."));
    }

    @Test
    void deveRemoverArquivoDeUploadAoExcluirAnuncio() throws Exception {
        MockMultipartFile imagem = imagemTeste("relogio.png", "image/png");

        mockMvc.perform(multipart("/anuncios")
                .file(imagem)
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

        mockMvc.perform(post("/anuncios/{id}/excluir", anuncioId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        assertThat(Files.exists(arquivo)).isFalse();
    }

    private Long cadastrarAnuncio(
            String titulo,
            String descricao,
            String tipoAnuncio,
            Long categoriaId,
            String local) throws Exception {
        mockMvc.perform(post("/anuncios")
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

    private Map<String, Object> buscarAnuncio(Long id) {
        return jdbcTemplate.queryForMap("select * from anuncios where id = ?", id);
    }

    private MockMultipartFile imagemTeste(String nomeArquivo, String contentType) {
        return new MockMultipartFile(
                "imagemArquivo",
                nomeArquivo,
                contentType,
                new byte[] { 1, 2, 3, 4 });
    }
}
