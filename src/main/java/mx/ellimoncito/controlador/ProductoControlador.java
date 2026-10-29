package mx.ellimoncito.controlador;

import jakarta.validation.Valid;
import mx.ellimoncito.formulario.ProductoFormulario;
import mx.ellimoncito.servicio.ProductoServicio;
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
@RequestMapping("/productos")
public class ProductoControlador {

    private final ProductoServicio productoServicio;

    public ProductoControlador(ProductoServicio productoServicio) {
        this.productoServicio = productoServicio;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "") String texto,
                        @RequestParam(defaultValue = "0") Integer idCategoria, Model modelo) {
        modelo.addAttribute("productos", productoServicio.buscar(texto, idCategoria));
        modelo.addAttribute("categorias", productoServicio.categorias());
        modelo.addAttribute("texto", texto);
        modelo.addAttribute("idCategoria", idCategoria);
        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("productoFormulario", new ProductoFormulario());
        prepararFormulario(modelo, "Nuevo producto", "/productos");
        return "productos/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("productoFormulario") ProductoFormulario formulario,
                          BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                productoServicio.registrar(formulario);
                mensajes.addFlashAttribute("exito", "Producto registrado.");
                return "redirect:/productos";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        prepararFormulario(modelo, "Nuevo producto", "/productos");
        return "productos/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("productoFormulario", productoServicio.formularioDe(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/productos";
        }
        prepararFormulario(modelo, "Modificar producto", "/productos/" + id);
        return "productos/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("productoFormulario") ProductoFormulario formulario,
                             BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                productoServicio.modificar(id, formulario);
                mensajes.addFlashAttribute("exito", "Producto actualizado.");
                return "redirect:/productos";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        prepararFormulario(modelo, "Modificar producto", "/productos/" + id);
        return "productos/formulario";
    }

    @PostMapping("/{id}/baja")
    public String baja(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            productoServicio.darDeBaja(id);
            mensajes.addFlashAttribute("exito", "Producto dado de baja.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            productoServicio.reactivar(id);
            mensajes.addFlashAttribute("exito", "Producto reactivado.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/productos";
    }

    private void prepararFormulario(Model modelo, String titulo, String accion) {
        modelo.addAttribute("titulo", titulo);
        modelo.addAttribute("accion", accion);
        modelo.addAttribute("categorias", productoServicio.categorias());
        modelo.addAttribute("envases", productoServicio.envases());
    }
}
