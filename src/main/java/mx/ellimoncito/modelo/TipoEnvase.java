package mx.ellimoncito.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "tipo_envase")
public class TipoEnvase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_envase")
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 50, message = "El nombre admite máximo 50 caracteres.")
    @Column(nullable = false, length = 50)
    private String nombre;

    @NotNull(message = "El depósito es obligatorio.")
    @DecimalMin(value = "0.01", message = "El depósito debe ser mayor que cero.")
    @Digits(integer = 8, fraction = 2, message = "El depósito admite máximo 2 decimales.")
    @Column(name = "valor_deposito", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDeposito;

    @Column(name = "vacios_en_bodega", nullable = false)
    private Integer vaciosEnBodega = 0;

    @Column(nullable = false)
    private Boolean activo = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getValorDeposito() {
        return valorDeposito;
    }

    public void setValorDeposito(BigDecimal valorDeposito) {
        this.valorDeposito = valorDeposito;
    }

    public Integer getVaciosEnBodega() {
        return vaciosEnBodega;
    }

    public void setVaciosEnBodega(Integer vaciosEnBodega) {
        this.vaciosEnBodega = vaciosEnBodega;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
