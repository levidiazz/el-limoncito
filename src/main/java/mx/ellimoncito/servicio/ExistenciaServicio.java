package mx.ellimoncito.servicio;

import java.util.List;
import mx.ellimoncito.modelo.Categoria;
import mx.ellimoncito.modelo.Lote;
import mx.ellimoncito.modelo.Producto;
import mx.ellimoncito.repositorio.CategoriaRepositorio;
import mx.ellimoncito.repositorio.ExistenciaRepositorio;
import mx.ellimoncito.repositorio.ExistenciaRepositorio.ExistenciaProducto;
import mx.ellimoncito.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;

@Service
public class ExistenciaServicio {

    private final ExistenciaRepositorio existenciaRepositorio;
    private final CategoriaRepositorio categoriaRepositorio;
    private final ProductoRepositorio productoRepositorio;

    public ExistenciaServicio(ExistenciaRepositorio existenciaRepositorio, CategoriaRepositorio categoriaRepositorio,
                              ProductoRepositorio productoRepositorio) {
        this.existenciaRepositorio = existenciaRepositorio;
        this.categoriaRepositorio = categoriaRepositorio;
        this.productoRepositorio = productoRepositorio;
    }

    public List<ExistenciaProducto> consultar(String texto, Integer idCategoria) {
        return existenciaRepositorio.consultar(texto == null ? "" : texto.trim(), idCategoria == null ? 0 : idCategoria);
    }

    public List<ExistenciaProducto> bajoMinimo(List<ExistenciaProducto> existencias) {
        return existencias.stream().filter(e -> e.existencia() < e.stockMinimo()).toList();
    }

    public List<Categoria> categorias() {
        return categoriaRepositorio.findAllByOrderByNombreAsc();
    }

    public Producto producto(Integer idProducto) {
        return productoRepositorio.findById(idProducto)
                .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
    }

    public List<Lote> lotesVigentes(Integer idProducto) {
        return existenciaRepositorio.lotesVigentes(idProducto);
    }
}
