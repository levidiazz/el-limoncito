package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepositorio extends JpaRepository<Producto, Integer> {

    List<Producto> findByActivoTrueOrderByNombreAsc();

    boolean existsByCodigoBarrasIgnoreCase(String codigoBarras);

    boolean existsByCodigoBarrasIgnoreCaseAndIdNot(String codigoBarras, Integer id);

    @Query("""
            SELECT p FROM Producto p
            WHERE (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :texto, '%')))
              AND (:idCategoria = 0 OR p.categoria.id = :idCategoria)
            ORDER BY p.nombre ASC
            """)
    List<Producto> buscar(@Param("texto") String texto, @Param("idCategoria") Integer idCategoria);
}
