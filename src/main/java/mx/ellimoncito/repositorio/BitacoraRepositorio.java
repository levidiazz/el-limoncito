package mx.ellimoncito.repositorio;

import mx.ellimoncito.modelo.Bitacora;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BitacoraRepositorio extends JpaRepository<Bitacora, Integer> {
}
