package mx.ellimoncito.repositorio;

import java.util.Optional;
import mx.ellimoncito.modelo.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComprobanteRepositorio extends JpaRepository<Comprobante, Integer> {

    Optional<Comprobante> findByVentaId(Integer idVenta);

    Optional<Comprobante> findTopByOrderByIdDesc();
}
