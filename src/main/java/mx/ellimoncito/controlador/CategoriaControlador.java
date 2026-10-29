package mx.ellimoncito.controlador;

import jakarta.validation.Valid;
import mx.ellimoncito.modelo.Categoria;
import mx.ellimoncito.servicio.CategoriaServicio;
import mx.ellimoncito.servicio.ReglaNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categorias")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    public CategoriaControlador(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    @GetMapping
    public String lista(Model modelo) {
        modelo.addAttribute("categorias", categoriaServicio.listarConConteo());
        return "categorias/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model modelo) {
        modelo.addAttribute("categoria", new Categoria());
        modelo.addAttribute("titulo", "Nueva categoría");
        modelo.addAttribute("accion", "/categorias");
        return "categorias/formulario";
    }

    @PostMapping
    public String guardar(@Valid Categoria categoria, BindingResult resultado, Model modelo,
                          RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                categoriaServicio.registrar(categoria);
                mensajes.addFlashAttribute("exito", "Categoría registrada.");
                return "redirect:/categorias";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("titulo", "Nueva categoría");
        modelo.addAttribute("accion", "/categorias");
        return "categorias/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("categoria", categoriaServicio.buscar(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/categorias";
        }
        modelo.addAttribute("titulo", "Modificar categoría");
        modelo.addAttribute("accion", "/categorias/" + id);
        return "categorias/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @Valid Categoria categoria, BindingResult resultado,
                             Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                categoriaServicio.modificar(id, categoria);
                mensajes.addFlashAttribute("exito", "Categoría actualizada.");
                return "redirect:/categorias";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("titulo", "Modificar categoría");
        modelo.addAttribute("accion", "/categorias/" + id);
        return "categorias/formulario";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            categoriaServicio.eliminar(id);
            mensajes.addFlashAttribute("exito", "Categoría eliminada.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/categorias";
    }
}
