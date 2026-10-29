package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.Lote;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface ExistenciaRepositorio extends Repository<Lote, Integer> {

    record ExistenciaProducto(Integer idProducto, String codigoBarras, String nombre, String presentacion,
                              String categoria, Integer stockMinimo, Long existencia) {
    }

    @Query("""
            SELECT new mx.ellimoncito.repositorio.ExistenciaRepositorio$ExistenciaProducto(
                p.id, p.codigoBarras, p.nombre, p.presentacion, c.nombre, p.stockMinimo,
                COALESCE(SUM(l.cantidadDisponible), 0L))
            FROM Producto p
            JOIN p.categoria c
            LEFT JOIN Lote l ON l.producto.id = p.id
            WHERE p.activo = TRUE
              AND (LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR LOWER(p.codigoBarras) LIKE LOWER(CONCAT('%', :texto, '%')))
              AND (:idCategoria = 0 OR c.id = :idCategoria)
            GROUP BY p.id, p.codigoBarras, p.nombre, p.presentacion, c.nombre, p.stockMinimo
            ORDER BY p.nombre ASC
            """)
    List<ExistenciaProducto> consultar(@Param("texto") String texto, @Param("idCategoria") Integer idCategoria);

    @Query("""
            SELECT l FROM Lote l
            WHERE l.producto.id = :idProducto AND l.cantidadDisponible > 0
            ORDER BY CASE WHEN l.fechaCaducidad IS NULL THEN 1 ELSE 0 END, l.fechaCaducidad ASC, l.fechaEntrada ASC
            """)
    List<Lote> lotesVigentes(@Param("idProducto") Integer idProducto);
}
