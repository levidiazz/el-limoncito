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
import java.time.LocalDateTime;

@Entity
@Table(name = "movimiento_envase")
public class MovimientoEnvase {

    public static final String ENTREGA_PROVEEDOR = "ENTREGA_PROVEEDOR";
    public static final String COBRO_DEPOSITO = "COBRO_DEPOSITO";
    public static final String DEVOLUCION_CLIENTE = "DEVOLUCION_CLIENTE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mov_envase")
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_tipo_envase")
    private TipoEnvase tipoEnvase;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(name = "id_venta")
    private Integer idVenta;

    @Column(name = "id_cliente")
    private Integer idCliente;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto = BigDecimal.ZERO;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TipoEnvase getTipoEnvase() {
        return tipoEnvase;
    }

    public void setTipoEnvase(TipoEnvase tipoEnvase) {
        this.tipoEnvase = tipoEnvase;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Integer getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(Integer idVenta) {
        this.idVenta = idVenta;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTipoLegible() {
        return switch (tipo) {
            case ENTREGA_PROVEEDOR -> "Entrega a proveedor";
            case COBRO_DEPOSITO -> "Cobro de depósito";
            case DEVOLUCION_CLIENTE -> "Devolución de cliente";
            default -> tipo;
        };
    }
}
