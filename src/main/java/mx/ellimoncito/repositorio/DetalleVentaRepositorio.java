package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleVentaRepositorio extends JpaRepository<DetalleVenta, Integer> {

    List<DetalleVenta> findByVentaIdOrderByIdAsc(Integer idVenta);
}
