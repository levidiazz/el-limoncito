package mx.ellimoncito.repositorio;

import java.util.List;
import java.util.Optional;
import mx.ellimoncito.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByUsuario(String usuario);

    boolean existsByUsuarioIgnoreCase(String usuario);

    boolean existsByUsuarioIgnoreCaseAndIdNot(String usuario, Integer id);

    List<Usuario> findByNombreCompletoContainingIgnoreCaseOrderByNombreCompletoAsc(String nombre);
}
