package mx.ellimoncito.servicio;

import java.math.BigDecimal;
import java.util.List;
import mx.ellimoncito.formulario.ProductoFormulario;
import mx.ellimoncito.modelo.Categoria;
import mx.ellimoncito.modelo.Producto;
import mx.ellimoncito.modelo.TipoEnvase;
import mx.ellimoncito.repositorio.CategoriaRepositorio;
import mx.ellimoncito.repositorio.ProductoRepositorio;
import mx.ellimoncito.repositorio.TipoEnvaseRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductoServicio {

    private final ProductoRepositorio productoRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final TipoEnvaseRepositorio tipoEnvaseRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public ProductoServicio(ProductoRepositorio productoRepositorio, CategoriaRepositorio categoriaRepositorio,
                            TipoEnvaseRepositorio tipoEnvaseRepositorio, BitacoraServicio bitacoraServicio) {
        this.productoRepositorio = productoRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.tipoEnvaseRepositorio = tipoEnvaseRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    public List<Producto> buscar(String texto, Integer idCategoria) {
        return productoRepositorio.buscar(texto == null ? "" : texto.trim(), idCategoria == null ? 0 : idCategoria);
    }

    public Producto buscarPorId(Integer id) {
        return productoRepositorio.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
    }

    public List<Categoria> categorias() {
        return categoriaRepositorio.findAllByOrderByNombreAsc();
    }

    public List<TipoEnvase> envases() {
        return tipoEnvaseRepositorio.findByActivoTrueOrderByNombreAsc();
    }

    public ProductoFormulario formularioDe(Integer id) {
        Producto producto = buscarPorId(id);
        ProductoFormulario formulario = new ProductoFormulario();
        formulario.setId(producto.getId());
        formulario.setCodigoBarras(producto.getCodigoBarras());
        formulario.setNombre(producto.getNombre());
        formulario.setPresentacion(producto.getPresentacion());
        formulario.setIdCategoria(producto.getCategoria().getId());
        formulario.setPrecioMenudeo(producto.getPrecioMenudeo());
        formulario.setPrecioMayoreo(producto.getPrecioMayoreo());
        formulario.setCantidadMinMayoreo(producto.getCantidadMinMayoreo());
        formulario.setStockMinimo(producto.getStockMinimo());
        formulario.setRetornable(producto.getRetornable());
        formulario.setIdTipoEnvase(producto.getTipoEnvase() == null ? null : producto.getTipoEnvase().getId());
        return formulario;
    }

    @Transactional
    public Producto registrar(ProductoFormulario formulario) {
        String codigo = formulario.getCodigoBarras().trim();
        if (productoRepositorio.existsByCodigoBarrasIgnoreCase(codigo)) {
            throw new ReglaNegocioException("Ya existe un producto con ese código de barras.");
        }
        validarPrecios(formulario);
        Producto producto = new Producto();
        aplicar(producto, formulario);
        producto.setActivo(true);
        Producto guardado = productoRepositorio.save(producto);
        bitacoraServicio.registrar("PRODUCTO_ALTA",
                "Se registró el producto " + guardado.getNombre() + " (" + guardado.getCodigoBarras() + ").");
        return guardado;
    }

    @Transactional
    public Producto modificar(Integer id, ProductoFormulario formulario) {
        Producto producto = buscarPorId(id);
        String codigo = formulario.getCodigoBarras().trim();
        if (productoRepositorio.existsByCodigoBarrasIgnoreCaseAndIdNot(codigo, id)) {
            throw new ReglaNegocioException("Ya existe un producto con ese código de barras.");
        }
        validarPrecios(formulario);
        BigDecimal menudeoAnterior = producto.getPrecioMenudeo();
        BigDecimal mayoreoAnterior = producto.getPrecioMayoreo();
        aplicar(producto, formulario);
        Producto guardado = productoRepositorio.save(producto);
        bitacoraServicio.registrar("PRODUCTO_MODIFICACION", "Se modificó el producto " + guardado.getNombre() + ".");
        if (menudeoAnterior.compareTo(guardado.getPrecioMenudeo()) != 0
                || mayoreoAnterior.compareTo(guardado.getPrecioMayoreo()) != 0) {
            bitacoraServicio.registrar("PRODUCTO_CAMBIO_PRECIO",
                    guardado.getNombre() + ": menudeo " + menudeoAnterior + " a " + guardado.getPrecioMenudeo()
                            + ", mayoreo " + mayoreoAnterior + " a " + guardado.getPrecioMayoreo() + ".");
        }
        return guardado;
    }

    @Transactional
    public void darDeBaja(Integer id) {
        Producto producto = buscarPorId(id);
        producto.setActivo(false);
        productoRepositorio.save(producto);
        bitacoraServicio.registrar("PRODUCTO_BAJA", "Se dio de baja el producto " + producto.getNombre() + ".");
    }

    @Transactional
    public void reactivar(Integer id) {
        Producto producto = buscarPorId(id);
        producto.setActivo(true);
        productoRepositorio.save(producto);
        bitacoraServicio.registrar("PRODUCTO_REACTIVACION", "Se reactivó el producto " + producto.getNombre() + ".");
    }

    private void validarPrecios(ProductoFormulario formulario) {
        if (formulario.getPrecioMayoreo().compareTo(formulario.getPrecioMenudeo()) > 0) {
            throw new ReglaNegocioException("El precio de mayoreo no puede ser mayor que el de menudeo.");
        }
        if (formulario.isRetornable() && formulario.getIdTipoEnvase() == null) {
            throw new ReglaNegocioException("Un producto retornable necesita un tipo de envase.");
        }
    }

    private void aplicar(Producto producto, ProductoFormulario formulario) {
        producto.setCodigoBarras(formulario.getCodigoBarras().trim());
        producto.setNombre(formulario.getNombre().trim());
        producto.setPresentacion(formulario.getPresentacion().trim());
        producto.setCategoria(categoriaRepositorio.findById(formulario.getIdCategoria())
                .orElseThrow(() -> new ReglaNegocioException("La categoría no existe.")));
        producto.setPrecioMenudeo(formulario.getPrecioMenudeo());
        producto.setPrecioMayoreo(formulario.getPrecioMayoreo());
        producto.setCantidadMinMayoreo(formulario.getCantidadMinMayoreo());
        producto.setStockMinimo(formulario.getStockMinimo());
        producto.setRetornable(formulario.isRetornable());
        if (formulario.isRetornable()) {
            producto.setTipoEnvase(tipoEnvaseRepositorio.findById(formulario.getIdTipoEnvase())
                    .orElseThrow(() -> new ReglaNegocioException("El tipo de envase no existe.")));
        } else {
            producto.setTipoEnvase(null);
        }
    }
}
