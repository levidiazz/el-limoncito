package mx.ellimoncito.controlador;

import jakarta.validation.Valid;
import mx.ellimoncito.formulario.ClienteFormulario;
import mx.ellimoncito.servicio.ClienteServicio;
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
@RequestMapping("/clientes")
public class ClienteControlador {

    private final ClienteServicio clienteServicio;

    public ClienteControlador(ClienteServicio clienteServicio) {
        this.clienteServicio = clienteServicio;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "") String texto, Model modelo) {
        modelo.addAttribute("clientes", clienteServicio.listar(texto));
        modelo.addAttribute("texto", texto);
        return "clientes/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("clienteFormulario", new ClienteFormulario());
        prepararFormulario(modelo, "Nuevo cliente", "/clientes");
        return "clientes/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("clienteFormulario") ClienteFormulario formulario,
                          BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                clienteServicio.registrar(formulario);
                mensajes.addFlashAttribute("exito", "Cliente registrado.");
                return "redirect:/clientes";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        prepararFormulario(modelo, "Nuevo cliente", "/clientes");
        return "clientes/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("clienteFormulario", clienteServicio.formularioDe(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/clientes";
        }
        prepararFormulario(modelo, "Modificar cliente", "/clientes/" + id);
        return "clientes/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("clienteFormulario") ClienteFormulario formulario,
                             BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                clienteServicio.modificar(id, formulario);
                mensajes.addFlashAttribute("exito", "Cliente actualizado.");
                return "redirect:/clientes";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        prepararFormulario(modelo, "Modificar cliente", "/clientes/" + id);
        return "clientes/formulario";
    }

    @PostMapping("/{id}/baja")
    public String baja(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            clienteServicio.darDeBaja(id);
            mensajes.addFlashAttribute("exito", "Cliente dado de baja. Sus ventas anteriores se conservan.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            clienteServicio.reactivar(id);
            mensajes.addFlashAttribute("exito", "Cliente reactivado.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/clientes";
    }

    private void prepararFormulario(Model modelo, String titulo, String accion) {
        modelo.addAttribute("titulo", titulo);
        modelo.addAttribute("accion", accion);
    }
}
