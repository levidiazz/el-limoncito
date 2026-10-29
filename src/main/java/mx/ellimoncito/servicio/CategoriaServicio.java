package mx.ellimoncito.servicio;

import java.util.List;
import mx.ellimoncito.modelo.Categoria;
import mx.ellimoncito.repositorio.CategoriaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaServicio {

    public record CategoriaConConteo(Categoria categoria, long productos) {
    }

    private final CategoriaRepositorio categoriaRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public CategoriaServicio(CategoriaRepositorio categoriaRepositorio, BitacoraServicio bitacoraServicio) {
        this.categoriaRepositorio = categoriaRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    public List<Categoria> listar() {
        return categoriaRepositorio.findAllByOrderByNombreAsc();
    }

    public List<CategoriaConConteo> listarConConteo() {
        return categoriaRepositorio.findAllByOrderByNombreAsc().stream()
                .map(c -> new CategoriaConConteo(c, categoriaRepositorio.contarProductosActivos(c.getId())))
                .toList();
    }

    public Categoria buscar(Integer id) {
        return categoriaRepositorio.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La categoría no existe."));
    }

    @Transactional
    public Categoria registrar(Categoria datos) {
        String nombre = datos.getNombre().trim();
        if (categoriaRepositorio.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre.");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(nombre);
        Categoria guardada = categoriaRepositorio.save(categoria);
        bitacoraServicio.registrar("CATEGORIA_ALTA", "Se registró la categoría " + guardada.getNombre() + ".");
        return guardada;
    }

    @Transactional
    public Categoria modificar(Integer id, Categoria datos) {
        Categoria categoria = buscar(id);
        String nombre = datos.getNombre().trim();
        if (categoriaRepositorio.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe una categoría con ese nombre.");
        }
        String anterior = categoria.getNombre();
        categoria.setNombre(nombre);
        Categoria guardada = categoriaRepositorio.save(categoria);
        bitacoraServicio.registrar("CATEGORIA_MODIFICACION",
                "La categoría " + anterior + " ahora se llama " + guardada.getNombre() + ".");
        return guardada;
    }

    @Transactional
    public void eliminar(Integer id) {
        Categoria categoria = buscar(id);
        if (categoriaRepositorio.contarProductos(id) > 0) {
            throw new ReglaNegocioException("La categoría tiene productos y no se puede eliminar.");
        }
        categoriaRepositorio.delete(categoria);
        bitacoraServicio.registrar("CATEGORIA_BAJA", "Se eliminó la categoría " + categoria.getNombre() + ".");
    }
}
