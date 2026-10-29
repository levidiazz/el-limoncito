package mx.ellimoncito.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioFormulario {

    private Integer id;

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(max = 100, message = "El nombre admite máximo 100 caracteres.")
    private String nombreCompleto;

    @NotBlank(message = "El usuario es obligatorio.")
    @Size(min = 4, max = 30, message = "El usuario debe tener entre 4 y 30 caracteres.")
    @Pattern(regexp = "[A-Za-z0-9._-]*", message = "El usuario solo admite letras, números, punto, guion y guion bajo.")
    private String usuario;

    @NotNull(message = "Selecciona un rol.")
    private Integer idRol;

    private String contrasena;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}
