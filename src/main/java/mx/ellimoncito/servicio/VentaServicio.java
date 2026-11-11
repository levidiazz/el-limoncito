package mx.ellimoncito.servicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import mx.ellimoncito.modelo.DetalleVenta;
import mx.ellimoncito.modelo.Lote;
import mx.ellimoncito.modelo.MovimientoInventario;
import mx.ellimoncito.modelo.Venta;
import mx.ellimoncito.repositorio.DetalleVentaRepositorio;
import mx.ellimoncito.repositorio.LotePepsRepositorio;
import mx.ellimoncito.repositorio.MovimientoInventarioRepositorio;
import mx.ellimoncito.repositorio.VentaRepositorio;
import mx.ellimoncito.servicio.CarritoServicio.CarritoVista;
import mx.ellimoncito.servicio.CarritoServicio.LineaCarrito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VentaServicio {

    private final CarritoServicio carritoServicio;
    private final VentaRepositorio ventaRepositorio;
    private final DetalleVentaRepositorio detalleVentaRepositorio;
    private final LotePepsRepositorio lotePepsRepositorio;
    private final MovimientoInventarioRepositorio movimientoInventarioRepositorio;
    private final UsuarioActualServicio usuarioActualServicio;
    private final BitacoraServicio bitacoraServicio;
    private final ApplicationEventPublisher publicador;

    public VentaServicio(CarritoServicio carritoServicio, VentaRepositorio ventaRepositorio,
                         DetalleVentaRepositorio detalleVentaRepositorio, LotePepsRepositorio lotePepsRepositorio,
                         MovimientoInventarioRepositorio movimientoInventarioRepositorio,
                         UsuarioActualServicio usuarioActualServicio, BitacoraServicio bitacoraServicio,
                         ApplicationEventPublisher publicador) {
        this.carritoServicio = carritoServicio;
        this.ventaRepositorio = ventaRepositorio;
        this.detalleVentaRepositorio = detalleVentaRepositorio;
        this.lotePepsRepositorio = lotePepsRepositorio;
        this.movimientoInventarioRepositorio = movimientoInventarioRepositorio;
        this.usuarioActualServicio = usuarioActualServicio;
        this.bitacoraServicio = bitacoraServicio;
        this.publicador = publicador;
    }

    public List<Venta> ultimasVentas() {
        return ventaRepositorio.findTop50ByOrderByFechaHoraDesc();
    }

    public Venta buscar(Integer id) {
        return ventaRepositorio.findById(id).orElseThrow(() -> new ReglaNegocioException("La venta no existe."));
    }

    public List<DetalleVenta> detalleDe(Integer idVenta) {
        return detalleVentaRepositorio.findByVentaIdOrderByIdAsc(idVenta);
    }

    @Transactional
    public Venta cobrar(BigDecimal efectivoRecibido) {
        CarritoVista carrito = carritoServicio.vista();
        if (carrito.vacio()) {
            throw new ReglaNegocioException("El carrito está vacío.");
        }
        if (efectivoRecibido == null || efectivoRecibido.signum() < 0) {
            throw new ReglaNegocioException("Captura el efectivo recibido.");
        }
        if (efectivoRecibido.compareTo(carrito.total()) < 0) {
            throw new ReglaNegocioException("El efectivo recibido no cubre el total de $" + carrito.total() + ".");
        }
        BigDecimal cambio = efectivoRecibido.subtract(carrito.total());

        Venta venta = new Venta();
        venta.setCliente(carrito.cliente());
        venta.setUsuario(usuarioActualServicio.obtener());
        venta.setFechaHora(LocalDateTime.now());
        venta.setSubtotal(carrito.subtotal());
        venta.setIva(carrito.iva());
        venta.setDepositoEnvases(carrito.deposito());
        venta.setTotal(carrito.total());
        venta.setEfectivoRecibido(efectivoRecibido);
        venta.setCambio(cambio);
        ventaRepositorio.save(venta);

        for (LineaCarrito linea : carrito.lineas()) {
            guardarDetalle(venta, linea);
            descontarPorPeps(venta, linea);
        }
        publicador.publishEvent(new VentaRegistrada(venta, carrito));
        bitacoraServicio.registrar("VENTA_REGISTRADA",
                "Venta " + venta.getId() + " por $" + venta.getTotal() + " con " + carrito.unidades() + " unidades.");
        carritoServicio.vaciar();
        return venta;
    }

    private void guardarDetalle(Venta venta, LineaCarrito linea) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(linea.producto());
        detalle.setCantidad(linea.cantidad());
        detalle.setPrecioAplicado(linea.precio());
        detalle.setTipoPrecio(linea.tipoPrecio());
        detalle.setImporte(linea.importe());
        detalleVentaRepositorio.save(detalle);
    }

    private void descontarPorPeps(Venta venta, LineaCarrito linea) {
        int pendiente = linea.cantidad();
        List<Lote> lotes = lotePepsRepositorio.lotesEnOrdenPeps(linea.producto().getId());
        long disponible = lotes.stream().mapToLong(Lote::getCantidadDisponible).sum();
        if (pendiente > disponible) {
            throw new ReglaNegocioException("Solo hay " + disponible + " unidades de " + linea.producto().getNombre() + ".");
        }
        for (Lote lote : lotes) {
            if (pendiente == 0) {
                break;
            }
            int descontar = Math.min(pendiente, lote.getCantidadDisponible());
            lote.setCantidadDisponible(lote.getCantidadDisponible() - descontar);
            lotePepsRepositorio.save(lote);
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setLote(lote);
            movimiento.setUsuario(venta.getUsuario());
            movimiento.setIdVenta(venta.getId());
            movimiento.setTipo(MovimientoInventario.VENTA);
            movimiento.setCantidad(descontar);
            movimiento.setMotivo("Venta " + venta.getId());
            movimiento.setFechaHora(venta.getFechaHora());
            movimientoInventarioRepositorio.save(movimiento);
            pendiente -= descontar;
        }
    }
}
