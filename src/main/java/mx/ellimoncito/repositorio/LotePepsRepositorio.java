package mx.ellimoncito.repositorio;

import jakarta.persistence.LockModeType;
import java.util.List;
import mx.ellimoncito.modelo.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LotePepsRepositorio extends JpaRepository<Lote, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT l FROM Lote l
            WHERE l.producto.id = :idProducto AND l.cantidadDisponible > 0
            ORDER BY CASE WHEN l.fechaCaducidad IS NULL THEN 1 ELSE 0 END, l.fechaCaducidad ASC, l.fechaEntrada ASC, l.id ASC
            """)
    List<Lote> lotesEnOrdenPeps(@Param("idProducto") Integer idProducto);

    @Query("SELECT COALESCE(SUM(l.cantidadDisponible), 0L) FROM Lote l WHERE l.producto.id = :idProducto")
    long existenciaDe(@Param("idProducto") Integer idProducto);
}
