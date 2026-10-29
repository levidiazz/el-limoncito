package mx.ellimoncito.formulario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EntradaFormulario {

    @NotNull(message = "Selecciona un proveedor.")
    private Integer idProveedor;

    @NotBlank(message = "El folio de la nota es obligatorio.")
    @Size(max = 30, message = "El folio admite máximo 30 caracteres.")
    private String folioNota;

    public Integer getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(Integer idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getFolioNota() {
        return folioNota;
    }

    public void setFolioNota(String folioNota) {
        this.folioNota = folioNota;
    }
}
