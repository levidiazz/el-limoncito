package mx.ellimoncito.repositorio;

import java.time.LocalDateTime;
import java.util.List;
import mx.ellimoncito.modelo.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MermaRepositorio extends JpaRepository<MovimientoInventario, Integer> {

    @Query("""
            SELECT m FROM MovimientoInventario m
            WHERE m.tipo = 'MERMA'
              AND (:motivo = '' OR m.motivo = :motivo)
              AND m.fechaHora >= :desde AND m.fechaHora < :hasta
            ORDER BY m.fechaHora DESC, m.id DESC
            """)
    List<MovimientoInventario> buscar(@Param("motivo") String motivo,
                                      @Param("desde") LocalDateTime desde,
                                      @Param("hasta") LocalDateTime hasta);
}
