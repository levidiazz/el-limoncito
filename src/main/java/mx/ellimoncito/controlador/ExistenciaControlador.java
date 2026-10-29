package mx.ellimoncito.controlador;

import mx.ellimoncito.repositorio.ExistenciaRepositorio.ExistenciaProducto;
import mx.ellimoncito.servicio.ExistenciaServicio;
import mx.ellimoncito.servicio.ReglaNegocioException;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/existencias")
public class ExistenciaControlador {

    private final ExistenciaServicio existenciaServicio;

    public ExistenciaControlador(ExistenciaServicio existenciaServicio) {
        this.existenciaServicio = existenciaServicio;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "") String texto,
                        @RequestParam(defaultValue = "0") Integer idCategoria, Model modelo) {
        List<ExistenciaProducto> existencias = existenciaServicio.consultar(texto, idCategoria);
        modelo.addAttribute("existencias", existencias);
        modelo.addAttribute("bajoMinimo", existenciaServicio.bajoMinimo(existencias));
        modelo.addAttribute("categorias", existenciaServicio.categorias());
        modelo.addAttribute("texto", texto);
        modelo.addAttribute("idCategoria", idCategoria);
        return "existencias/lista";
    }

    @GetMapping("/{idProducto}/lotes")
    public String lotes(@PathVariable Integer idProducto, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("producto", existenciaServicio.producto(idProducto));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/existencias";
        }
        modelo.addAttribute("lotes", existenciaServicio.lotesVigentes(idProducto));
        return "existencias/lotes";
    }
}
