package mx.ellimoncito.servicio;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import mx.ellimoncito.modelo.MovimientoEnvase;
import mx.ellimoncito.modelo.TipoEnvase;
import mx.ellimoncito.repositorio.MovimientoEnvaseRepositorio;
import mx.ellimoncito.servicio.CarritoServicio.LineaCarrito;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DepositoEnvaseServicio {

    private static final class Acumulado {
        private final TipoEnvase tipo;
        private int cantidad;
        private BigDecimal monto = BigDecimal.ZERO;

        private Acumulado(TipoEnvase tipo) {
            this.tipo = tipo;
        }
    }

    private final MovimientoEnvaseRepositorio movimientoEnvaseRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public DepositoEnvaseServicio(MovimientoEnvaseRepositorio movimientoEnvaseRepositorio,
                                  BitacoraServicio bitacoraServicio) {
        this.movimientoEnvaseRepositorio = movimientoEnvaseRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    @EventListener
    @Transactional
    public void alRegistrarseVenta(VentaRegistrada evento) {
        if (evento.carrito().deposito().signum() == 0) {
            return;
        }
        if (evento.carrito().cliente() == null) {
            throw new ReglaNegocioException("La venta incluye envases retornables. Elige un cliente para registrar su depósito.");
        }
        Map<Integer, Acumulado> porTipo = new LinkedHashMap<>();
        for (LineaCarrito linea : evento.carrito().lineas()) {
            TipoEnvase tipo = linea.producto().getTipoEnvase();
            if (linea.deposito().signum() > 0 && tipo != null) {
                Acumulado acumulado = porTipo.computeIfAbsent(tipo.getId(), k -> new Acumulado(tipo));
                acumulado.cantidad += linea.cantidad();
                acumulado.monto = acumulado.monto.add(linea.deposito());
            }
        }
        for (Acumulado acumulado : porTipo.values()) {
            MovimientoEnvase movimiento = new MovimientoEnvase();
            movimiento.setTipoEnvase(acumulado.tipo);
            movimiento.setUsuario(evento.venta().getUsuario());
            movimiento.setIdVenta(evento.venta().getId());
            movimiento.setIdCliente(evento.carrito().cliente().getId());
            movimiento.setTipo(MovimientoEnvase.COBRO_DEPOSITO);
            movimiento.setCantidad(acumulado.cantidad);
            movimiento.setMonto(acumulado.monto);
            movimiento.setFechaHora(evento.venta().getFechaHora());
            movimientoEnvaseRepositorio.save(movimiento);
        }
        bitacoraServicio.registrar("DEPOSITO_COBRADO",
                "Depósito de envases por $" + evento.carrito().deposito() + " a nombre de "
                        + evento.carrito().cliente().getNombre() + " en la venta " + evento.venta().getId() + ".");
    }
}
