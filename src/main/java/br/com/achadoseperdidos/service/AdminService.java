package br.com.achadoseperdidos.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.achadoseperdidos.dto.AdminDashboardDto;
import br.com.achadoseperdidos.model.StatusAnuncio;
import br.com.achadoseperdidos.model.TipoAnuncio;
import br.com.achadoseperdidos.repository.AnuncioRepository;
import br.com.achadoseperdidos.repository.UsuarioRepository;

/**
 * Fornece os dados consolidados usados pela area administrativa.
 */
@Service
public class AdminService {

    private final AnuncioRepository anuncioRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Cria o servico com os repositorios usados nos indicadores administrativos.
     *
     * @param anuncioRepository repositorio de anuncios
     * @param usuarioRepository repositorio de usuarios
     */
    public AdminService(AnuncioRepository anuncioRepository, UsuarioRepository usuarioRepository) {
        this.anuncioRepository = anuncioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Calcula os indicadores e carrega os anuncios para a tela do administrador.
     *
     * @return dados completos do painel administrativo
     */
    @Transactional(readOnly = true)
    public AdminDashboardDto obterDashboard() {
        return new AdminDashboardDto(
                usuarioRepository.count(),
                anuncioRepository.count(),
                anuncioRepository.countByStatus(StatusAnuncio.ATIVO),
                anuncioRepository.countByStatus(StatusAnuncio.RESOLVIDO),
                anuncioRepository.countByTipoAnuncio(TipoAnuncio.PERDIDO),
                anuncioRepository.countByTipoAnuncio(TipoAnuncio.ENCONTRADO),
                anuncioRepository.buscarTodosParaAdministracao());
    }
}
