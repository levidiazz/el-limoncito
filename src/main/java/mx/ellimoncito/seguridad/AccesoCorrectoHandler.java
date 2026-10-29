package mx.ellimoncito.seguridad;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import mx.ellimoncito.servicio.AutenticacionServicio;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class AccesoCorrectoHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AutenticacionServicio autenticacionServicio;

    public AccesoCorrectoHandler(AutenticacionServicio autenticacionServicio) {
        this.autenticacionServicio = autenticacionServicio;
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication autenticacion) throws IOException, ServletException {
        autenticacionServicio.registrarAccesoCorrecto(autenticacion.getName());
        super.onAuthenticationSuccess(request, response, autenticacion);
    }
}
