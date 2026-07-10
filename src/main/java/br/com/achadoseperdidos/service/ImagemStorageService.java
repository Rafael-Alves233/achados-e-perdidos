package br.com.achadoseperdidos.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servico responsavel por validar e salvar imagens enviadas pelos usuarios.
 */
@Service
public class ImagemStorageService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp");

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".gif",
            ".webp");

    private final Path uploadDir;

    /**
     * Configura o diretorio local usado para armazenar os arquivos enviados.
     *
     * @param uploadDir caminho configurado pela propriedade {@code app.upload-dir}
     */
    public ImagemStorageService(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    /**
     * Salva uma imagem enviada no formulario e retorna o caminho publico.
     *
     * @param arquivo arquivo enviado pelo usuario
     * @return caminho publico da imagem, quando o arquivo foi informado
     */
    public Optional<String> salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            return Optional.empty();
        }

        String nomeOriginal = StringUtils.cleanPath(
                Objects.requireNonNullElse(arquivo.getOriginalFilename(), "imagem"));
        String extensao = extrairExtensao(nomeOriginal);
        validarArquivo(arquivo, extensao);

        String nomeArquivo = UUID.randomUUID() + extensao;
        Path destino = uploadDir.resolve(nomeArquivo).normalize();

        if (!destino.startsWith(uploadDir)) {
            throw new IllegalArgumentException("Nome de arquivo invalido.");
        }

        try {
            Files.createDirectories(uploadDir);
            arquivo.transferTo(destino);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Nao foi possivel salvar a imagem.", exception);
        }

        return Optional.of("/uploads/" + nomeArquivo);
    }

    /**
     * Remove um arquivo de upload local quando o caminho pertence a /uploads.
     *
     * @param caminhoPublico caminho salvo no anuncio
     */
    public void remover(String caminhoPublico) {
        if (caminhoPublico == null || !caminhoPublico.startsWith("/uploads/")) {
            return;
        }

        String nomeArquivo = StringUtils.cleanPath(caminhoPublico.substring("/uploads/".length()));
        Path arquivo = uploadDir.resolve(nomeArquivo).normalize();

        if (!arquivo.startsWith(uploadDir)) {
            return;
        }

        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Nao foi possivel remover a imagem.", exception);
        }
    }

    private void validarArquivo(MultipartFile arquivo, String extensao) {
        String contentType = arquivo.getContentType();
        if (contentType == null || !TIPOS_PERMITIDOS.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Envie uma imagem JPG, PNG, GIF ou WEBP.");
        }

        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new IllegalArgumentException("Envie uma imagem JPG, PNG, GIF ou WEBP.");
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        int ultimoPonto = nomeArquivo.lastIndexOf('.');
        if (ultimoPonto < 0) {
            throw new IllegalArgumentException("Envie uma imagem JPG, PNG, GIF ou WEBP.");
        }
        return nomeArquivo.substring(ultimoPonto).toLowerCase(Locale.ROOT);
    }
}
