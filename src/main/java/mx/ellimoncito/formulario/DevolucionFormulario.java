package mx.ellimoncito.formulario;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class DevolucionFormulario {

    @NotNull(message = "Selecciona al cliente.")
    private Integer idCliente;

    @NotNull(message = "Selecciona el tipo de envase.")
    private Integer idTipoEnvase;

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero.")
    private Integer cantidad;

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdTipoEnvase() {
        return idTipoEnvase;
    }

    public void setIdTipoEnvase(Integer idTipoEnvase) {
        this.idTipoEnvase = idTipoEnvase;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
