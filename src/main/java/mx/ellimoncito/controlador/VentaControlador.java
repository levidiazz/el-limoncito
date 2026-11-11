package mx.ellimoncito.controlador;

import java.math.BigDecimal;
import mx.ellimoncito.modelo.Venta;
import mx.ellimoncito.servicio.ReglaNegocioException;
import mx.ellimoncito.servicio.VentaServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ventas")
public class VentaControlador {

    private final VentaServicio ventaServicio;

    public VentaControlador(VentaServicio ventaServicio) {
        this.ventaServicio = ventaServicio;
    }

    @GetMapping
    public String lista(Model modelo) {
        modelo.addAttribute("ventas", ventaServicio.ultimasVentas());
        return "ventas/lista";
    }

    @PostMapping("/cobrar")
    public String cobrar(@RequestParam(required = false) BigDecimal efectivoRecibido, RedirectAttributes mensajes) {
        try {
            Venta venta = ventaServicio.cobrar(efectivoRecibido);
            mensajes.addFlashAttribute("exito", "Venta registrada. Entrega el cambio al cliente.");
            return "redirect:/ventas/" + venta.getId();
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/nueva";
        }
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("venta", ventaServicio.buscar(id));
            modelo.addAttribute("detalles", ventaServicio.detalleDe(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas";
        }
        return "ventas/detalle";
    }
}
