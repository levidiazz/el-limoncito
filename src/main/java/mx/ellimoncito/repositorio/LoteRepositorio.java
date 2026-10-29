package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteRepositorio extends JpaRepository<Lote, Integer> {

    List<Lote> findByEntradaIdOrderByIdAsc(Integer idEntrada);
}
