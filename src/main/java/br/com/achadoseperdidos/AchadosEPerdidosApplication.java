package br.com.achadoseperdidos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal responsavel por iniciar a aplicacao Spring Boot do sistema
 * de Achados e Perdidos.
 */
@SpringBootApplication
public class AchadosEPerdidosApplication {

    /**
     * Ponto de entrada da aplicacao.
     *
     * @param args argumentos recebidos pela linha de comando
     */
    public static void main(String[] args) {
        SpringApplication.run(AchadosEPerdidosApplication.class, args);
    }
}
