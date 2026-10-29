package mx.ellimoncito.repositorio;

import mx.ellimoncito.modelo.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimientoInventarioRepositorio extends JpaRepository<MovimientoInventario, Integer> {

    void deleteByLoteId(Integer idLote);
}
