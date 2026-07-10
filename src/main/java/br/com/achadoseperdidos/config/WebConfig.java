package br.com.achadoseperdidos.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracao dos recursos estaticos adicionais da aplicacao.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path uploadDir;

    /**
     * Configura o caminho fisico que sera exposto pela rota de uploads.
     *
     * @param uploadDir caminho configurado pela propriedade {@code app.upload-dir}
     */
    public WebConfig(@Value("${app.upload-dir:uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadLocation());
    }

    private String uploadLocation() {
        String location = uploadDir.toUri().toString();
        return location.endsWith("/") ? location : location + "/";
    }
}
