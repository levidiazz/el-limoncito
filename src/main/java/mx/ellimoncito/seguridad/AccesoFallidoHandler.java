package mx.ellimoncito.seguridad;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.ellimoncito.servicio.AutenticacionServicio;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class AccesoFallidoHandler implements AuthenticationFailureHandler {

    private final AutenticacionServicio autenticacionServicio;

    public AccesoFallidoHandler(AutenticacionServicio autenticacionServicio) {
        this.autenticacionServicio = autenticacionServicio;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException excepcion) throws IOException {
        String destino = "/login?error";
        if (excepcion instanceof LockedException) {
            destino = "/login?bloqueado";
        } else if (excepcion instanceof DisabledException) {
            destino = "/login?inactivo";
        } else if (excepcion instanceof BadCredentialsException) {
            String nombreUsuario = request.getParameter("username");
            if (nombreUsuario != null && autenticacionServicio.registrarIntentoFallido(nombreUsuario.trim())) {
                destino = "/login?bloqueado";
            }
        }
        response.sendRedirect(request.getContextPath() + destino);
    }
}
