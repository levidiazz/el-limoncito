package mx.ellimoncito.repositorio;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import mx.ellimoncito.modelo.MovimientoEnvase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DevolucionRepositorio extends JpaRepository<MovimientoEnvase, Integer> {

    List<MovimientoEnvase> findByTipoAndIdClienteNotNullAndFechaHoraGreaterThanEqualOrderByFechaHoraDesc(
            String tipo, LocalDateTime desde);

    List<MovimientoEnvase> findByIdClienteAndTipoIn(Integer idCliente, Collection<String> tipos);

    List<MovimientoEnvase> findByIdClienteNotNullAndTipoIn(Collection<String> tipos);
}
