package mx.ellimoncito.servicio;

import java.time.LocalDateTime;
import java.util.List;
import mx.ellimoncito.modelo.Comprobante;
import mx.ellimoncito.modelo.DetalleVenta;
import mx.ellimoncito.modelo.Venta;
import mx.ellimoncito.repositorio.ComprobanteRepositorio;
import mx.ellimoncito.repositorio.DetalleVentaRepositorio;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ComprobanteServicio {

    private static final String PREFIJO = "LIM-";

    public record ComprobanteVista(Comprobante comprobante, Venta venta, List<DetalleVenta> detalles) {
    }

    private final ComprobanteRepositorio comprobanteRepositorio;
    private final DetalleVentaRepositorio detalleVentaRepositorio;

    public ComprobanteServicio(ComprobanteRepositorio comprobanteRepositorio,
                               DetalleVentaRepositorio detalleVentaRepositorio) {
        this.comprobanteRepositorio = comprobanteRepositorio;
        this.detalleVentaRepositorio = detalleVentaRepositorio;
    }

    @EventListener
    @Transactional
    public void alRegistrarseVenta(VentaRegistrada evento) {
        emitir(evento.venta());
    }

    @Transactional
    public Comprobante emitir(Venta venta) {
        Comprobante comprobante = new Comprobante();
        comprobante.setVenta(venta);
        comprobante.setFolio(siguienteFolio());
        comprobante.setFechaEmision(LocalDateTime.now());
        return comprobanteRepositorio.save(comprobante);
    }

    public ComprobanteVista consultar(Integer idVenta) {
        Comprobante comprobante = comprobanteRepositorio.findByVentaId(idVenta)
                .orElseThrow(() -> new ReglaNegocioException("La venta no tiene comprobante."));
        Venta venta = comprobante.getVenta();
        return new ComprobanteVista(comprobante, venta, detalleVentaRepositorio.findByVentaIdOrderByIdAsc(idVenta));
    }

    private String siguienteFolio() {
        int ultimo = comprobanteRepositorio.findTopByOrderByIdDesc()
                .map(c -> Integer.parseInt(c.getFolio().substring(PREFIJO.length())))
                .orElse(0);
        return PREFIJO + String.format("%06d", ultimo + 1);
    }
}
