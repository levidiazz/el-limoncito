package mx.ellimoncito.servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import mx.ellimoncito.formulario.MermaFormulario;
import mx.ellimoncito.modelo.Lote;
import mx.ellimoncito.modelo.MovimientoInventario;
import mx.ellimoncito.modelo.Producto;
import mx.ellimoncito.repositorio.LotePepsRepositorio;
import mx.ellimoncito.repositorio.MermaRepositorio;
import mx.ellimoncito.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MermaServicio {

    public static final List<String> MOTIVOS = List.of("Caducado", "Dañado", "Robo o faltante", "Otro");

    private static final LocalDate FECHA_MINIMA = LocalDate.of(2000, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(2100, 1, 1);

    private final MermaRepositorio mermaRepositorio;
    private final LotePepsRepositorio lotePepsRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final UsuarioActualServicio usuarioActualServicio;
    private final BitacoraServicio bitacoraServicio;

    public MermaServicio(MermaRepositorio mermaRepositorio, LotePepsRepositorio lotePepsRepositorio,
                         ProductoRepositorio productoRepositorio, UsuarioActualServicio usuarioActualServicio,
                         BitacoraServicio bitacoraServicio) {
        this.mermaRepositorio = mermaRepositorio;
        this.lotePepsRepositorio = lotePepsRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.usuarioActualServicio = usuarioActualServicio;
        this.bitacoraServicio = bitacoraServicio;
    }

    public List<MovimientoInventario> listar(String motivo, LocalDate desde, LocalDate hasta) {
        LocalDate inicio = desde == null ? FECHA_MINIMA : desde;
        LocalDate fin = hasta == null ? FECHA_MAXIMA : hasta.plusDays(1);
        return mermaRepositorio.buscar(motivo == null ? "" : motivo, inicio.atStartOfDay(), fin.atStartOfDay());
    }

    public List<Producto> productosActivos() {
        return productoRepositorio.findByActivoTrueOrderByNombreAsc();
    }

    public MovimientoInventario buscar(Integer id) {
        MovimientoInventario merma = mermaRepositorio.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La merma no existe."));
        if (!MovimientoInventario.MERMA.equals(merma.getTipo())) {
            throw new ReglaNegocioException("El movimiento no es una merma.");
        }
        return merma;
    }

    @Transactional
    public void registrar(MermaFormulario formulario) {
        validarMotivo(formulario.getMotivo());
        Producto producto = productoRepositorio.findById(formulario.getIdProducto())
                .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
        int pendiente = formulario.getCantidad();
        long existencia = lotePepsRepositorio.existenciaDe(producto.getId());
        if (pendiente > existencia) {
            throw new ReglaNegocioException("Solo hay " + existencia + " unidades de " + producto.getNombre() + ".");
        }
        List<Lote> lotes = lotePepsRepositorio.lotesEnOrdenPeps(producto.getId());
        for (Lote lote : lotes) {
            if (pendiente == 0) {
                break;
            }
            int descontar = Math.min(pendiente, lote.getCantidadDisponible());
            lote.setCantidadDisponible(lote.getCantidadDisponible() - descontar);
            lotePepsRepositorio.save(lote);
            guardarMovimiento(lote, descontar, formulario.getMotivo());
            pendiente -= descontar;
        }
        bitacoraServicio.registrar("MERMA_ALTA",
                "Merma de " + formulario.getCantidad() + " de " + producto.getNombre() + ". Motivo: " + formulario.getMotivo() + ".");
    }

    @Transactional
    public void corregir(Integer id, Integer nuevaCantidad) {
        if (nuevaCantidad == null || nuevaCantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }
        MovimientoInventario merma = buscar(id);
        Lote lote = lotePepsRepositorio.findById(merma.getLote().getId())
                .orElseThrow(() -> new ReglaNegocioException("El lote no existe."));
        int diferencia = nuevaCantidad - merma.getCantidad();
        if (diferencia > lote.getCantidadDisponible()) {
            throw new ReglaNegocioException("El lote solo tiene " + lote.getCantidadDisponible()
                    + " unidades disponibles para aumentar la merma.");
        }
        lote.setCantidadDisponible(lote.getCantidadDisponible() - diferencia);
        lotePepsRepositorio.save(lote);
        int anterior = merma.getCantidad();
        merma.setCantidad(nuevaCantidad);
        mermaRepositorio.save(merma);
        bitacoraServicio.registrar("MERMA_CORRECCION",
                "Merma de " + lote.getProducto().getNombre() + " corregida de " + anterior + " a " + nuevaCantidad + ".");
    }

    @Transactional
    public void cancelar(Integer id) {
        MovimientoInventario merma = buscar(id);
        Lote lote = lotePepsRepositorio.findById(merma.getLote().getId())
                .orElseThrow(() -> new ReglaNegocioException("El lote no existe."));
        lote.setCantidadDisponible(lote.getCantidadDisponible() + merma.getCantidad());
        lotePepsRepositorio.save(lote);
        mermaRepositorio.delete(merma);
        bitacoraServicio.registrar("MERMA_CANCELACION",
                "Se canceló una merma de " + merma.getCantidad() + " de " + lote.getProducto().getNombre()
                        + ". Las unidades regresaron a su lote.");
    }

    private void guardarMovimiento(Lote lote, int cantidad, String motivo) {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setLote(lote);
        movimiento.setUsuario(usuarioActualServicio.obtener());
        movimiento.setTipo(MovimientoInventario.MERMA);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFechaHora(LocalDateTime.now());
        mermaRepositorio.save(movimiento);
    }

    private void validarMotivo(String motivo) {
        if (!MOTIVOS.contains(motivo)) {
            throw new ReglaNegocioException("El motivo seleccionado no es válido.");
        }
    }
}
