package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.MovimientoEnvase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoEnvaseRepositorio extends JpaRepository<MovimientoEnvase, Integer> {

    List<MovimientoEnvase> findTop20ByOrderByFechaHoraDesc();

    boolean existsByTipoEnvaseId(Integer idTipoEnvase);
}
