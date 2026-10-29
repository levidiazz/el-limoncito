package mx.ellimoncito.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "id_tipo_envase")
    private TipoEnvase tipoEnvase;

    @Column(name = "codigo_barras", nullable = false, unique = true, length = 30)
    private String codigoBarras;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String presentacion;

    @Column(name = "precio_menudeo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMenudeo;

    @Column(name = "precio_mayoreo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMayoreo;

    @Column(name = "cantidad_min_mayoreo", nullable = false)
    private Integer cantidadMinMayoreo;

    @Column(name = "tasa_iva", nullable = false, precision = 4, scale = 2)
    private BigDecimal tasaIva = new BigDecimal("0.16");

    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(nullable = false)
    private Boolean retornable = false;

    @Column(nullable = false)
    private Boolean activo = true;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public TipoEnvase getTipoEnvase() {
        return tipoEnvase;
    }

    public void setTipoEnvase(TipoEnvase tipoEnvase) {
        this.tipoEnvase = tipoEnvase;
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

    public BigDecimal getTasaIva() {
        return tasaIva;
    }

    public void setTasaIva(BigDecimal tasaIva) {
        this.tasaIva = tasaIva;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Boolean getRetornable() {
        return retornable;
    }

    public void setRetornable(Boolean retornable) {
        this.retornable = retornable;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
