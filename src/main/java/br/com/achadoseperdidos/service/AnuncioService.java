package br.com.achadoseperdidos.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.achadoseperdidos.model.Anuncio;
import br.com.achadoseperdidos.model.StatusAnuncio;
import br.com.achadoseperdidos.repository.AnuncioRepository;

/**
 * Servico responsavel pelas regras e operacoes relacionadas aos anuncios.
 */
@Service
public class AnuncioService {

    private final AnuncioRepository anuncioRepository;

    public AnuncioService(AnuncioRepository anuncioRepository) {
        this.anuncioRepository = anuncioRepository;
    }

    /**
     * Lista os anuncios que ainda estao ativos no sistema.
     *
     * @return anuncios ativos ordenados pela data mais recente
     */
    @Transactional(readOnly = true)
    public List<Anuncio> listarAtivos() {
        return anuncioRepository.findByStatusOrderByDataDesc(StatusAnuncio.ATIVO);
    }
}
