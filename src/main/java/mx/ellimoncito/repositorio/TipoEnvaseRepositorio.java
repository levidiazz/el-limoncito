package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.TipoEnvase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoEnvaseRepositorio extends JpaRepository<TipoEnvase, Integer> {

    List<TipoEnvase> findAllByOrderByNombreAsc();

    List<TipoEnvase> findByActivoTrueOrderByNombreAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);
}
