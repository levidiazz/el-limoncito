package mx.ellimoncito.controlador;

import mx.ellimoncito.servicio.UsuarioActualServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InicioControlador {

    private final UsuarioActualServicio usuarioActualServicio;

    public InicioControlador(UsuarioActualServicio usuarioActualServicio) {
        this.usuarioActualServicio = usuarioActualServicio;
    }

    @GetMapping("/")
    public String inicio(Model modelo) {
        modelo.addAttribute("usuario", usuarioActualServicio.obtener());
        return "inicio";
    }
}
