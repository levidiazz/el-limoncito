package mx.ellimoncito.repositorio;

import java.util.List;
import java.util.Optional;
import mx.ellimoncito.modelo.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepositorio extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);

    List<Rol> findAllByOrderByNombreAsc();
}
