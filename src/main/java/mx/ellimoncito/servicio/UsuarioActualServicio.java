package mx.ellimoncito.servicio;

import mx.ellimoncito.modelo.Usuario;
import mx.ellimoncito.repositorio.UsuarioRepositorio;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioActualServicio {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioActualServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    public Usuario obtener() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || autenticacion instanceof AnonymousAuthenticationToken) {
            throw new ReglaNegocioException("No hay una sesión activa.");
        }
        return usuarioRepositorio.findByUsuario(autenticacion.getName())
                .orElseThrow(() -> new ReglaNegocioException("El usuario de la sesión no existe."));
    }
}
