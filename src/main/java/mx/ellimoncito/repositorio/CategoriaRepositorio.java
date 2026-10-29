package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Integer> {

    List<Categoria> findAllByOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

    @Query(value = "SELECT COUNT(*) FROM producto WHERE id_categoria = :id AND activo", nativeQuery = true)
    long contarProductosActivos(@Param("id") Integer id);

    @Query(value = "SELECT COUNT(*) FROM producto WHERE id_categoria = :id", nativeQuery = true)
    long contarProductos(@Param("id") Integer id);
}
