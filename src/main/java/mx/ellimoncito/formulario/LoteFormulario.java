package mx.ellimoncito.formulario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public class LoteFormulario {

    @NotNull(message = "Selecciona un producto.")
    private Integer idProducto;

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero.")
    private Integer cantidad;

    @NotNull(message = "El costo unitario es obligatorio.")
    @DecimalMin(value = "0.00", message = "El costo no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El costo admite máximo 2 decimales.")
    private BigDecimal costoUnitario;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaCaducidad;

    public Integer getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Integer idProducto) {
        this.idProducto = idProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public void setCostoUnitario(BigDecimal costoUnitario) {
        this.costoUnitario = costoUnitario;
    }

    public LocalDate getFechaCaducidad() {
        return fechaCaducidad;
    }

    public void setFechaCaducidad(LocalDate fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
    }
}
