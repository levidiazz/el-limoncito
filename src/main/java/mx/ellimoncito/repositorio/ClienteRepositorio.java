package mx.ellimoncito.repositorio;

import java.util.List;
import mx.ellimoncito.modelo.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteRepositorio extends JpaRepository<Cliente, Integer> {

    @Query("""
            SELECT c FROM Cliente c
            WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR COALESCE(c.telefono, '') LIKE CONCAT('%', :texto, '%')
            ORDER BY c.nombre ASC
            """)
    List<Cliente> buscar(@Param("texto") String texto);

    @Query("""
            SELECT c FROM Cliente c
            WHERE c.activo = TRUE
              AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                   OR COALESCE(c.telefono, '') LIKE CONCAT('%', :texto, '%'))
            ORDER BY c.nombre ASC
            """)
    List<Cliente> buscarActivos(@Param("texto") String texto);

    boolean existsByTelefono(String telefono);

    boolean existsByTelefonoAndIdNot(String telefono, Integer id);
}
