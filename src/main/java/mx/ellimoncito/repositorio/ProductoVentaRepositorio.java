package mx.ellimoncito.repositorio;

import java.util.List;
import java.util.Optional;
import mx.ellimoncito.modelo.Producto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ProductoVentaRepositorio extends Repository<Producto, Integer> {

    @Query("SELECT p FROM Producto p WHERE p.activo = TRUE AND LOWER(p.codigoBarras) = LOWER(:codigo)")
    Optional<Producto> buscarPorCodigo(@Param("codigo") String codigo);

    @Query("""
            SELECT p FROM Producto p
            WHERE p.activo = TRUE
              AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :texto, '%')))
            ORDER BY p.nombre ASC
            """)
    List<Producto> buscarActivos(@Param("texto") String texto);
}
