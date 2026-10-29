package mx.ellimoncito.repositorio;

import java.time.LocalDateTime;
import java.util.List;
import mx.ellimoncito.modelo.EntradaMercancia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntradaMercanciaRepositorio extends JpaRepository<EntradaMercancia, Integer> {

    boolean existsByProveedorIdAndFolioNotaIgnoreCase(Integer idProveedor, String folioNota);

    @Query("""
            SELECT e FROM EntradaMercancia e
            WHERE (:idProveedor = 0 OR e.proveedor.id = :idProveedor)
              AND e.fechaEntrada >= :desde AND e.fechaEntrada < :hasta
            ORDER BY e.fechaEntrada DESC
            """)
    List<EntradaMercancia> buscar(@Param("idProveedor") Integer idProveedor,
                                  @Param("desde") LocalDateTime desde,
                                  @Param("hasta") LocalDateTime hasta);
}
