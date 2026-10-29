package mx.ellimoncito.controlador;

import mx.ellimoncito.servicio.CuentaServicio;
import mx.ellimoncito.servicio.ReglaNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cuenta")
public class CuentaControlador {

    private final CuentaServicio cuentaServicio;

    public CuentaControlador(CuentaServicio cuentaServicio) {
        this.cuentaServicio = cuentaServicio;
    }

    @GetMapping("/contrasena")
    public String formulario() {
        return "cuenta/contrasena";
    }

    @PostMapping("/contrasena")
    public String cambiar(@RequestParam String actual, @RequestParam String nueva,
                          @RequestParam String confirmacion, RedirectAttributes mensajes) {
        try {
            cuentaServicio.cambiarContrasena(actual, nueva, confirmacion);
            mensajes.addFlashAttribute("exito", "Contraseña actualizada.");
            return "redirect:/";
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cuenta/contrasena";
        }
    }
}
