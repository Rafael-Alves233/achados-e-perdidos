package br.com.achadoseperdidos.dto;

import br.com.achadoseperdidos.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO utilizado para editar dados basicos do perfil do usuario autenticado.
 */
public class UsuarioPerfilDto {

    @NotBlank(message = "Informe o nome.")
    @Size(max = 120, message = "O nome deve ter no maximo 120 caracteres.")
    private String nome;

    public static UsuarioPerfilDto from(Usuario usuario) {
        UsuarioPerfilDto dto = new UsuarioPerfilDto();
        dto.setNome(usuario.getNome());
        return dto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
