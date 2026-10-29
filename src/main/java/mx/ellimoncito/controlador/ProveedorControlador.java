package mx.ellimoncito.controlador;

import jakarta.validation.Valid;
import mx.ellimoncito.formulario.ProveedorFormulario;
import mx.ellimoncito.servicio.ProveedorServicio;
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
@RequestMapping("/proveedores")
public class ProveedorControlador {

    private final ProveedorServicio proveedorServicio;

    public ProveedorControlador(ProveedorServicio proveedorServicio) {
        this.proveedorServicio = proveedorServicio;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "") String texto, Model modelo) {
        modelo.addAttribute("proveedores", proveedorServicio.listar(texto));
        modelo.addAttribute("texto", texto);
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("proveedorFormulario", new ProveedorFormulario());
        modelo.addAttribute("titulo", "Nuevo proveedor");
        modelo.addAttribute("accion", "/proveedores");
        return "proveedores/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("proveedorFormulario") ProveedorFormulario formulario,
                          BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (resultado.hasErrors()) {
            modelo.addAttribute("titulo", "Nuevo proveedor");
            modelo.addAttribute("accion", "/proveedores");
            return "proveedores/formulario";
        }
        proveedorServicio.registrar(formulario);
        mensajes.addFlashAttribute("exito", "Proveedor registrado.");
        return "redirect:/proveedores";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("proveedorFormulario", proveedorServicio.formularioDe(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/proveedores";
        }
        modelo.addAttribute("titulo", "Modificar proveedor");
        modelo.addAttribute("accion", "/proveedores/" + id);
        return "proveedores/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("proveedorFormulario") ProveedorFormulario formulario,
                             BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                proveedorServicio.modificar(id, formulario);
                mensajes.addFlashAttribute("exito", "Proveedor actualizado.");
                return "redirect:/proveedores";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("titulo", "Modificar proveedor");
        modelo.addAttribute("accion", "/proveedores/" + id);
        return "proveedores/formulario";
    }

    @PostMapping("/{id}/baja")
    public String baja(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            proveedorServicio.darDeBaja(id);
            mensajes.addFlashAttribute("exito", "Proveedor dado de baja.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedores";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            proveedorServicio.reactivar(id);
            mensajes.addFlashAttribute("exito", "Proveedor reactivado.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/proveedores";
    }
}
