package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentaRepositorio extends JpaRepository<Venta, Integer> {

    List<Venta> findTop50ByOrderByFechaHoraDesc();
}
