package br.com.achadoseperdidos.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class ImagemStorageServiceTest {

    @TempDir
    private Path uploadDir;

    // Teste unitario: salva imagem valida no diretorio configurado.
    @Test
    void deveSalvarImagemPngValida() throws Exception {
        Object service = novoImagemStorageService();
        MockMultipartFile imagem = new MockMultipartFile(
                "imagemArquivo",
                "cracha.png",
                "image/png",
                new byte[] { 1, 2, 3 });

        String caminhoPublico = (String) salvar(service, imagem).orElseThrow();
        Path arquivoSalvo = uploadDir.resolve(caminhoPublico.substring("/uploads/".length()));

        assertThat(caminhoPublico)
                .startsWith("/uploads/")
                .endsWith(".png");
        assertThat(Files.exists(arquivoSalvo)).isTrue();
    }

    // Teste unitario: nenhum arquivo enviado nao deve gerar caminho de upload.
    @Test
    void deveRetornarVazioQuandoArquivoNaoFoiInformado() throws Exception {
        Object service = novoImagemStorageService();

        assertThat(salvar(service, null)).isEmpty();
    }

    // Teste unitario: arquivo vazio tambem nao deve ser salvo.
    @Test
    void deveRetornarVazioQuandoArquivoEstaVazio() throws Exception {
        Object service = novoImagemStorageService();
        MockMultipartFile imagemVazia = new MockMultipartFile(
                "imagemArquivo",
                "foto.png",
                "image/png",
                new byte[0]);

        assertThat(salvar(service, imagemVazia)).isEmpty();
    }

    // Teste unitario: rejeita arquivo que nao e imagem permitida.
    @Test
    void deveRejeitarArquivoComTipoInvalido() throws Exception {
        Object service = novoImagemStorageService();
        MockMultipartFile arquivo = new MockMultipartFile(
                "imagemArquivo",
                "arquivo.txt",
                "text/plain",
                new byte[] { 1, 2, 3 });

        assertThatThrownBy(() -> salvar(service, arquivo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Envie uma imagem JPG, PNG, GIF ou WEBP.");
    }

    // Teste unitario: content type valido nao basta se a extensao for invalida.
    @Test
    void deveRejeitarArquivoComExtensaoInvalida() throws Exception {
        Object service = novoImagemStorageService();
        MockMultipartFile arquivo = new MockMultipartFile(
                "imagemArquivo",
                "arquivo.txt",
                "image/png",
                new byte[] { 1, 2, 3 });

        assertThatThrownBy(() -> salvar(service, arquivo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Envie uma imagem JPG, PNG, GIF ou WEBP.");
    }

    // Teste unitario: arquivo sem extensao nao pode ser aceito.
    @Test
    void deveRejeitarArquivoSemExtensao() throws Exception {
        Object service = novoImagemStorageService();
        MockMultipartFile arquivo = new MockMultipartFile(
                "imagemArquivo",
                "arquivo",
                "image/png",
                new byte[] { 1, 2, 3 });

        assertThatThrownBy(() -> salvar(service, arquivo))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Envie uma imagem JPG, PNG, GIF ou WEBP.");
    }

    // Teste unitario: remover caminho de upload apaga o arquivo fisico.
    @Test
    void deveRemoverArquivoDeUpload() throws Exception {
        Object service = novoImagemStorageService();
        Path arquivo = uploadDir.resolve("foto.png");
        Files.write(arquivo, new byte[] { 1, 2, 3 });

        remover(service, "/uploads/foto.png");

        assertThat(Files.exists(arquivo)).isFalse();
    }

    // Teste unitario: caminhos que nao sao de upload sao ignorados.
    @Test
    void deveIgnorarCaminhoForaDeUploadsAoRemover() throws Exception {
        Object service = novoImagemStorageService();
        Path arquivo = uploadDir.resolve("demo.png");
        Files.write(arquivo, new byte[] { 1, 2, 3 });

        remover(service, "/images/demo.png");

        assertThat(Files.exists(arquivo)).isTrue();
    }

    // Usa reflexao por uma limitacao local ao compilar testes unitarios que importam services diretamente.
    private Object novoImagemStorageService() throws Exception {
        Class<?> serviceClass = Class.forName("br.com.achadoseperdidos.service.ImagemStorageService");
        Constructor<?> constructor = serviceClass.getConstructor(String.class);
        return constructor.newInstance(uploadDir.toString());
    }

    private Optional<?> salvar(Object service, MockMultipartFile arquivo) throws Exception {
        Method salvar = service.getClass().getMethod("salvar", org.springframework.web.multipart.MultipartFile.class);
        try {
            return (Optional<?>) salvar.invoke(service, arquivo);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw exception;
        }
    }

    private void remover(Object service, String caminhoPublico) throws Exception {
        Method remover = service.getClass().getMethod("remover", String.class);
        try {
            remover.invoke(service, caminhoPublico);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw exception;
        }
    }
}
