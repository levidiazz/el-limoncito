package mx.ellimoncito.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProveedorFormulario {

    @NotBlank(message = "El nombre del proveedor es obligatorio.")
    @Size(max = 100, message = "El nombre admite máximo 100 caracteres.")
    private String nombre;

    @Size(max = 100, message = "El contacto admite máximo 100 caracteres.")
    private String contacto;

    @Pattern(regexp = "[0-9]{0,10}", message = "El teléfono solo admite números, máximo 10 dígitos.")
    private String telefono;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}
