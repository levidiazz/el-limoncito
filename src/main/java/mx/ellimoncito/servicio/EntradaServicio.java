package mx.ellimoncito.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import mx.ellimoncito.formulario.EntradaFormulario;
import mx.ellimoncito.formulario.LoteFormulario;
import mx.ellimoncito.modelo.EntradaMercancia;
import mx.ellimoncito.modelo.Lote;
import mx.ellimoncito.modelo.MovimientoInventario;
import mx.ellimoncito.modelo.Producto;
import mx.ellimoncito.modelo.Proveedor;
import mx.ellimoncito.modelo.Usuario;
import mx.ellimoncito.repositorio.EntradaMercanciaRepositorio;
import mx.ellimoncito.repositorio.LoteRepositorio;
import mx.ellimoncito.repositorio.MovimientoInventarioRepositorio;
import mx.ellimoncito.repositorio.ProductoRepositorio;
import mx.ellimoncito.repositorio.ProveedorRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EntradaServicio {

    private static final LocalDate FECHA_MINIMA = LocalDate.of(2000, 1, 1);
    private static final LocalDate FECHA_MAXIMA = LocalDate.of(2100, 1, 1);

    private final EntradaMercanciaRepositorio entradaRepositorio;
    private final LoteRepositorio loteRepositorio;
    private final MovimientoInventarioRepositorio movimientoRepositorio;
    private final ProveedorRepositorio proveedorRepositorio;
    private final ProductoRepositorio productoRepositorio;
    private final UsuarioActualServicio usuarioActualServicio;
    private final BitacoraServicio bitacoraServicio;

    public EntradaServicio(EntradaMercanciaRepositorio entradaRepositorio, LoteRepositorio loteRepositorio,
                           MovimientoInventarioRepositorio movimientoRepositorio,
                           ProveedorRepositorio proveedorRepositorio, ProductoRepositorio productoRepositorio,
                           UsuarioActualServicio usuarioActualServicio, BitacoraServicio bitacoraServicio) {
        this.entradaRepositorio = entradaRepositorio;
        this.loteRepositorio = loteRepositorio;
        this.movimientoRepositorio = movimientoRepositorio;
        this.proveedorRepositorio = proveedorRepositorio;
        this.productoRepositorio = productoRepositorio;
        this.usuarioActualServicio = usuarioActualServicio;
        this.bitacoraServicio = bitacoraServicio;
    }

    public List<EntradaMercancia> listar(Integer idProveedor, LocalDate desde, LocalDate hasta) {
        LocalDate inicio = desde == null ? FECHA_MINIMA : desde;
        LocalDate fin = hasta == null ? FECHA_MAXIMA : hasta.plusDays(1);
        return entradaRepositorio.buscar(idProveedor == null ? 0 : idProveedor,
                inicio.atStartOfDay(), fin.atStartOfDay());
    }

    public List<Proveedor> proveedores() {
        return proveedorRepositorio.findAllByOrderByNombreAsc();
    }

    public List<Proveedor> proveedoresActivos() {
        return proveedorRepositorio.findByActivoTrueOrderByNombreAsc();
    }

    public List<Producto> productosActivos() {
        return productoRepositorio.findByActivoTrueOrderByNombreAsc();
    }

    public EntradaMercancia buscar(Integer id) {
        return entradaRepositorio.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La entrada no existe."));
    }

    public Lote buscarLote(Integer id) {
        return loteRepositorio.findById(id).orElseThrow(() -> new ReglaNegocioException("El lote no existe."));
    }

    public List<Lote> lotesDe(Integer idEntrada) {
        return loteRepositorio.findByEntradaIdOrderByIdAsc(idEntrada);
    }

    public LoteFormulario formularioDeLote(Integer idLote) {
        Lote lote = buscarLote(idLote);
        LoteFormulario formulario = new LoteFormulario();
        formulario.setIdProducto(lote.getProducto().getId());
        formulario.setCantidad(lote.getCantidadInicial());
        formulario.setCostoUnitario(lote.getCostoUnitario());
        formulario.setFechaCaducidad(lote.getFechaCaducidad());
        return formulario;
    }

    @Transactional
    public EntradaMercancia registrar(EntradaFormulario formulario) {
        Proveedor proveedor = proveedorRepositorio.findById(formulario.getIdProveedor())
                .orElseThrow(() -> new ReglaNegocioException("El proveedor no existe."));
        if (!proveedor.getActivo()) {
            throw new ReglaNegocioException("El proveedor está dado de baja.");
        }
        String folio = formulario.getFolioNota().trim();
        if (entradaRepositorio.existsByProveedorIdAndFolioNotaIgnoreCase(proveedor.getId(), folio)) {
            throw new ReglaNegocioException("Ya se registró una entrada con ese folio para este proveedor.");
        }
        EntradaMercancia entrada = new EntradaMercancia();
        entrada.setProveedor(proveedor);
        entrada.setUsuario(usuarioActualServicio.obtener());
        entrada.setFolioNota(folio);
        entrada.setFechaEntrada(LocalDateTime.now());
        entrada.setTotal(BigDecimal.ZERO);
        EntradaMercancia guardada = entradaRepositorio.save(entrada);
        bitacoraServicio.registrar("ENTRADA_ALTA",
                "Se registró la entrada " + guardada.getFolioNota() + " del proveedor " + proveedor.getNombre() + ".");
        return guardada;
    }

    @Transactional
    public Lote agregarLote(Integer idEntrada, LoteFormulario formulario) {
        EntradaMercancia entrada = buscar(idEntrada);
        Producto producto = productoRepositorio.findById(formulario.getIdProducto())
                .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
        if (!producto.getActivo()) {
            throw new ReglaNegocioException("El producto está dado de baja.");
        }
        validarCaducidad(formulario.getFechaCaducidad());
        Lote lote = new Lote();
        lote.setEntrada(entrada);
        lote.setProducto(producto);
        lote.setCantidadInicial(formulario.getCantidad());
        lote.setCantidadDisponible(formulario.getCantidad());
        lote.setCostoUnitario(formulario.getCostoUnitario());
        lote.setFechaEntrada(entrada.getFechaEntrada());
        lote.setFechaCaducidad(formulario.getFechaCaducidad());
        Lote guardado = loteRepositorio.save(lote);

        registrarMovimiento(guardado, MovimientoInventario.ENTRADA, formulario.getCantidad(),
                "Entrada " + entrada.getFolioNota());
        recalcularTotal(entrada);
        bitacoraServicio.registrar("ENTRADA_LOTE",
                "Entrada " + entrada.getFolioNota() + ": " + formulario.getCantidad() + " de " + producto.getNombre() + ".");
        return guardado;
    }

    @Transactional
    public Lote corregirLote(Integer idLote, LoteFormulario formulario) {
        Lote lote = buscarLote(idLote);
        exigirLoteIntacto(lote, "corregir");
        Producto producto = productoRepositorio.findById(formulario.getIdProducto())
                .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
        validarCaducidad(formulario.getFechaCaducidad());
        int diferencia = formulario.getCantidad() - lote.getCantidadInicial();
        lote.setProducto(producto);
        lote.setCantidadInicial(formulario.getCantidad());
        lote.setCantidadDisponible(formulario.getCantidad());
        lote.setCostoUnitario(formulario.getCostoUnitario());
        lote.setFechaCaducidad(formulario.getFechaCaducidad());
        Lote guardado = loteRepositorio.save(lote);
        if (diferencia != 0) {
            registrarMovimiento(guardado, MovimientoInventario.AJUSTE, Math.abs(diferencia),
                    "Corrección de entrada " + lote.getEntrada().getFolioNota()
                            + (diferencia > 0 ? " (aumento)" : " (disminución)"));
        }
        recalcularTotal(lote.getEntrada());
        bitacoraServicio.registrar("ENTRADA_CORRECCION",
                "Se corrigió un lote de " + producto.getNombre() + " en la entrada " + lote.getEntrada().getFolioNota() + ".");
        return guardado;
    }

    @Transactional
    public void eliminarLote(Integer idLote) {
        Lote lote = buscarLote(idLote);
        exigirLoteIntacto(lote, "quitar");
        EntradaMercancia entrada = lote.getEntrada();
        String producto = lote.getProducto().getNombre();
        movimientoRepositorio.deleteByLoteId(idLote);
        loteRepositorio.delete(lote);
        loteRepositorio.flush();
        recalcularTotal(entrada);
        bitacoraServicio.registrar("ENTRADA_LOTE_QUITADO",
                "Se quitó un lote de " + producto + " de la entrada " + entrada.getFolioNota() + ".");
    }

    @Transactional
    public void cancelar(Integer idEntrada) {
        EntradaMercancia entrada = buscar(idEntrada);
        List<Lote> lotes = loteRepositorio.findByEntradaIdOrderByIdAsc(idEntrada);
        for (Lote lote : lotes) {
            exigirLoteIntacto(lote, "cancelar la entrada con");
        }
        for (Lote lote : lotes) {
            movimientoRepositorio.deleteByLoteId(lote.getId());
        }
        loteRepositorio.deleteAll(lotes);
        loteRepositorio.flush();
        entradaRepositorio.delete(entrada);
        bitacoraServicio.registrar("ENTRADA_CANCELACION",
                "Se canceló la entrada " + entrada.getFolioNota() + " del proveedor " + entrada.getProveedor().getNombre() + ".");
    }

    private void exigirLoteIntacto(Lote lote, String accion) {
        if (!lote.isIntacto()) {
            throw new ReglaNegocioException("No se puede " + accion
                    + " un lote de " + lote.getProducto().getNombre() + " porque ya tiene unidades vendidas o dadas de baja.");
        }
    }

    private void validarCaducidad(LocalDate caducidad) {
        if (caducidad != null && caducidad.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de caducidad no puede ser anterior a hoy.");
        }
    }

    private void registrarMovimiento(Lote lote, String tipo, int cantidad, String motivo) {
        Usuario usuario = usuarioActualServicio.obtener();
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setLote(lote);
        movimiento.setUsuario(usuario);
        movimiento.setTipo(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFechaHora(LocalDateTime.now());
        movimientoRepositorio.save(movimiento);
    }

    private void recalcularTotal(EntradaMercancia entrada) {
        BigDecimal total = loteRepositorio.findByEntradaIdOrderByIdAsc(entrada.getId()).stream()
                .map(l -> l.getCostoUnitario().multiply(BigDecimal.valueOf(l.getCantidadInicial())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        entrada.setTotal(total);
        entradaRepositorio.save(entrada);
    }
}
