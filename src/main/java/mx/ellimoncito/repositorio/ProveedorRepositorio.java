package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepositorio extends JpaRepository<Proveedor, Integer> {

    List<Proveedor> findByNombreContainingIgnoreCaseOrderByNombreAsc(String nombre);

    List<Proveedor> findByActivoTrueOrderByNombreAsc();

    List<Proveedor> findAllByOrderByNombreAsc();
}
