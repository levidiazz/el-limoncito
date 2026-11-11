package mx.ellimoncito.controlador;

import mx.ellimoncito.servicio.ComprobanteServicio;
import mx.ellimoncito.servicio.ReglaNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ventas")
public class ComprobanteControlador {

    private final ComprobanteServicio comprobanteServicio;

    public ComprobanteControlador(ComprobanteServicio comprobanteServicio) {
        this.comprobanteServicio = comprobanteServicio;
    }

    @GetMapping("/{id}/comprobante")
    public String comprobante(@PathVariable Integer id, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("vista", comprobanteServicio.consultar(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas";
        }
        return "ventas/comprobante";
    }
}
