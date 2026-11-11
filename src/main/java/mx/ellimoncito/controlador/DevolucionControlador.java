package mx.ellimoncito.controlador;

import jakarta.validation.Valid;
import java.util.List;
import mx.ellimoncito.formulario.DevolucionFormulario;
import mx.ellimoncito.servicio.DevolucionServicio;
import mx.ellimoncito.servicio.DevolucionServicio.DevolucionDelDia;
import mx.ellimoncito.servicio.ReglaNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/devoluciones")
public class DevolucionControlador {

    private final DevolucionServicio devolucionServicio;

    public DevolucionControlador(DevolucionServicio devolucionServicio) {
        this.devolucionServicio = devolucionServicio;
    }

    @GetMapping
    public String lista(Model modelo) {
        List<DevolucionDelDia> devoluciones = devolucionServicio.devolucionesDelDia();
        modelo.addAttribute("devoluciones", devoluciones);
        modelo.addAttribute("totalReintegrado", devolucionServicio.totalReintegradoHoy(devoluciones));
        return "devoluciones/lista";
    }

    @GetMapping("/pendientes")
    public String pendientes(Model modelo) {
        modelo.addAttribute("clientes", devolucionServicio.clientesConPendientes());
        return "devoluciones/pendientes";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) Integer idCliente, Model modelo) {
        modelo.addAttribute("clientes", devolucionServicio.clientesConPendientes());
        if (idCliente != null) {
            DevolucionFormulario formulario = new DevolucionFormulario();
            formulario.setIdCliente(idCliente);
            prepararFormulario(modelo, formulario);
        }
        return "devoluciones/nueva";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("devolucionFormulario") DevolucionFormulario formulario,
                          BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                devolucionServicio.registrar(formulario);
                mensajes.addFlashAttribute("exito", "Devolución registrada. Reintegra el depósito en efectivo al cliente.");
                return "redirect:/devoluciones";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("clientes", devolucionServicio.clientesConPendientes());
        if (formulario.getIdCliente() != null) {
            prepararFormulario(modelo, formulario);
        }
        return "devoluciones/nueva";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            devolucionServicio.cancelar(id);
            mensajes.addFlashAttribute("exito", "Devolución cancelada. El depósito y los envases se revirtieron.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/devoluciones";
    }

    private void prepararFormulario(Model modelo, DevolucionFormulario formulario) {
        try {
            modelo.addAttribute("clienteElegido", devolucionServicio.cliente(formulario.getIdCliente()));
            modelo.addAttribute("pendientes", devolucionServicio.pendientesDelCliente(formulario.getIdCliente()));
            if (!modelo.containsAttribute("devolucionFormulario")) {
                modelo.addAttribute("devolucionFormulario", formulario);
            }
        } catch (ReglaNegocioException e) {
            modelo.addAttribute("error", e.getMessage());
        }
    }
}
