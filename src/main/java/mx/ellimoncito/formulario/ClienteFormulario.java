package mx.ellimoncito.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ClienteFormulario {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    @Size(max = 100, message = "El nombre admite máximo 100 caracteres.")
    private String nombre;

    @Pattern(regexp = "[0-9]{0,10}", message = "El teléfono solo admite números, máximo 10 dígitos.")
    private String telefono;

    @NotBlank(message = "Selecciona el perfil de precio.")
    @Pattern(regexp = "MINORISTA|MAYORISTA|DISTRIBUIDOR", message = "El perfil de precio no es válido.")
    private String perfil = "MINORISTA";

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }
}
