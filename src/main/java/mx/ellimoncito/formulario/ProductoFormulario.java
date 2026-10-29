package mx.ellimoncito.formulario;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ProductoFormulario {

    private Integer id;

    @NotBlank(message = "El código de barras es obligatorio.")
    @Size(max = 30, message = "El código admite máximo 30 caracteres.")
    @Pattern(regexp = "[A-Za-z0-9-]*", message = "El código solo admite letras, números y guion.")
    private String codigoBarras;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 100, message = "El nombre admite máximo 100 caracteres.")
    private String nombre;

    @NotBlank(message = "La presentación es obligatoria.")
    @Size(max = 50, message = "La presentación admite máximo 50 caracteres.")
    private String presentacion;

    @NotNull(message = "Selecciona una categoría.")
    private Integer idCategoria;

    @NotNull(message = "El precio de menudeo es obligatorio.")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 2 decimales.")
    private BigDecimal precioMenudeo;

    @NotNull(message = "El precio de mayoreo es obligatorio.")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    @Digits(integer = 8, fraction = 2, message = "El precio admite máximo 2 decimales.")
    private BigDecimal precioMayoreo;

    @NotNull(message = "La cantidad mínima de mayoreo es obligatoria.")
    @Min(value = 1, message = "La cantidad mínima de mayoreo debe ser al menos 1.")
    private Integer cantidadMinMayoreo = 12;

    @NotNull(message = "El stock mínimo es obligatorio.")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo.")
    private Integer stockMinimo = 0;

    private boolean retornable;

    private Integer idTipoEnvase;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public BigDecimal getPrecioMenudeo() {
        return precioMenudeo;
    }

    public void setPrecioMenudeo(BigDecimal precioMenudeo) {
        this.precioMenudeo = precioMenudeo;
    }

    public BigDecimal getPrecioMayoreo() {
        return precioMayoreo;
    }

    public void setPrecioMayoreo(BigDecimal precioMayoreo) {
        this.precioMayoreo = precioMayoreo;
    }

    public Integer getCantidadMinMayoreo() {
        return cantidadMinMayoreo;
    }

    public void setCantidadMinMayoreo(Integer cantidadMinMayoreo) {
        this.cantidadMinMayoreo = cantidadMinMayoreo;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isRetornable() {
        return retornable;
    }

    public void setRetornable(boolean retornable) {
        this.retornable = retornable;
    }

    public Integer getIdTipoEnvase() {
        return idTipoEnvase;
    }

    public void setIdTipoEnvase(Integer idTipoEnvase) {
        this.idTipoEnvase = idTipoEnvase;
    }
}
