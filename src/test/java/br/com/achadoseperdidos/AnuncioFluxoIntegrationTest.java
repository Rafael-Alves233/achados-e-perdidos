package br.com.achadoseperdidos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AnuncioFluxoIntegrationTest {

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
}
